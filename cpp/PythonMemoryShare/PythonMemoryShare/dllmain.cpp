// dllmain.cpp : Defines the entry point for the DLL application.
#include "pch.h"
#ifdef _DEBUG
#undef _DEBUG
#define _RESTORE_DEBUG
#endif
#include <Python.h>
#ifdef _RESTORE_DEBUG
#undef _RESTORE_DEBUG
#define _DEBUG
#endif
#include <string>
#include <map>
#include <comdef.h>
#include <tchar.h>

using namespace std;

map<string, HANDLE> fileHandles;
map<string, LPTSTR> viewHandles;

PyObject* sharedMemoryError;

static TCHAR* ANSItoUnicode(const char* text) {
    wchar_t* out = new wchar_t[strlen(text) + 1];
    size_t ncc = 0;
    mbstowcs_s(&ncc, out, strlen(text) + 1, text, _TRUNCATE);
    return reinterpret_cast<TCHAR*>(out);
}

static char* UnicodetoANSI(wchar_t* text) {
    char* out = new char[2 * (wcslen(text) + 1)];
    size_t ncc = 0;
    wcstombs_s(&ncc, out, 2 * (wcslen(text) + 1), text, _TRUNCATE);
    return out;
}

static CHAR* ANSItoANSI(const char* text) {
    return const_cast<CHAR*>(text);
}

#ifdef UNICODE
#define CP(text) ANSItoUnicode(text)
#else
#define CP(text) ANSItoANSI(text)
#endif

#ifdef UNICODE
#define CPI(text) UnicodetoANSI(text)
#else
#define CPI(text) ANSItoANSI(text)
#endif

//Credit: https://stackoverflow.com/questions/440133/how-do-i-create-a-random-alpha-numeric-string-in-c
static void gen_random(char* s, const int len) {
    static const char alphanum[] =
        "0123456789"
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        "abcdefghijklmnopqrstuvwxyz";

    for (int i = 0; i < len; ++i) {
        s[i] = alphanum[rand() % (sizeof(alphanum) - 1)];
    }

    s[len] = 0;
}

//Credit: https://stackoverflow.com/questions/2709713/how-to-convert-unsigned-long-to-string
static string sul(unsigned long num) {
    return to_string(num);
}

static const char* GetLastErrorExport() {
    string out = "Error Occured: " + sul(GetLastError());
    return out.c_str();
}

static string randomString() {
    char* randChars = new char[32];
    gen_random(randChars, 32);
    return string(randChars);
}

static PyObject* PySharedMemory_Create(PyObject* self, PyObject* args) {
    const char* name;
    size_t bufSize;
    if (!PyArg_ParseTuple(args, "sI", &name, &bufSize)) {
        return NULL;
    }
    HANDLE memoryHandle = CreateFileMappingA(INVALID_HANDLE_VALUE, NULL, PAGE_READWRITE, 0, bufSize, name);
    if (memoryHandle == NULL) {
        PyErr_SetString(sharedMemoryError, GetLastErrorExport());
        return NULL;
    }
    string key;
    do {
        key = randomString();
    } while (fileHandles.count(key) != 0);
    fileHandles.insert(make_pair(key, memoryHandle));
    return PyUnicode_FromString(key.c_str());
}

static PyObject* PySharedMemory_Open(PyObject* self, PyObject* args) {
    const char* name;
    if (!PyArg_ParseTuple(args, "s", &name)) {
        return NULL;
    }
    HANDLE memoryHandle = OpenFileMappingA(FILE_MAP_ALL_ACCESS, FALSE, name);
    if (memoryHandle == NULL) {
        PyErr_SetString(sharedMemoryError, GetLastErrorExport());
        return NULL;
    }
    string key;
    do {
        key = randomString();
    } while (fileHandles.count(key) != 0);
    fileHandles.insert(make_pair(key, memoryHandle));
    return PyUnicode_FromString(key.c_str());
}

static PyObject* PySharedMemory_Read(PyObject* self, PyObject* args) {
    const char* handleHandle;
    if (!PyArg_ParseTuple(args, "s", &handleHandle)) {
        return NULL;
    }
    if (fileHandles.count(string(handleHandle)) != 1) {
        PyErr_SetString(sharedMemoryError, "Handle Does Not Exist");
        return NULL;
    }
    LPTSTR data = (LPTSTR)MapViewOfFile(fileHandles.find(string(handleHandle))->second, FILE_MAP_ALL_ACCESS, 0, 0, 0);
    if (data == NULL) {
        PyErr_SetString(sharedMemoryError, GetLastErrorExport());
        return NULL;
    }
    PyObject* out = PyUnicode_FromString(CPI(data));
    UnmapViewOfFile(data);
    return out;
}

static PyObject* PySharedMemory_Write(PyObject* self, PyObject* args) {
    const char* handleHandle;
    const char* text;
    if (!PyArg_ParseTuple(args, "ss", &handleHandle, &text)) {
        return NULL;
    }
    if (fileHandles.count(string(handleHandle)) != 1) {
        PyErr_SetString(sharedMemoryError, "Handle Does Not Exist");
        return NULL;
    }
    LPTSTR data = (LPTSTR)MapViewOfFile(fileHandles.find(string(handleHandle))->second, FILE_MAP_ALL_ACCESS, 0, 0, 0);
    if (data == NULL) {
        PyErr_SetString(sharedMemoryError, GetLastErrorExport());
        return NULL;
    }
    TCHAR* msg = CP(text);
    CopyMemory((PVOID)data, msg, (_tcslen(msg) * sizeof(TCHAR)));
    string key;
    do {
        key = randomString();
    } while (viewHandles.count(key) != 0);
    viewHandles.insert(make_pair(key, data));
    return PyUnicode_FromString(key.c_str());
}

static PyObject* PySharedMemory_UnmapView(PyObject* self, PyObject* args) {
    const char* handleHandle;
    if (!PyArg_ParseTuple(args, "s", &handleHandle)) {
        return NULL;
    }
    if (viewHandles.count(string(handleHandle)) != 1) {
        PyErr_SetString(sharedMemoryError, GetLastErrorExport());
        return NULL;
    }
    viewHandles.erase(string(handleHandle));
    UnmapViewOfFile(viewHandles.find(string(handleHandle))->second);
    Py_INCREF(Py_None);
    return Py_None;
}

static PyObject* PySharedMemory_Close(PyObject* self, PyObject* args) {
    const char* handleHandle;
    if (!PyArg_ParseTuple(args, "s", &handleHandle)) {
        return NULL;
    }
    if (fileHandles.count(string(handleHandle)) != 1) {
        PyErr_SetString(sharedMemoryError, "Handle Does Not Exist");
        return NULL;
    }
    CloseHandle(fileHandles.find(string(handleHandle))->second);
    fileHandles.erase(string(handleHandle));
    Py_INCREF(Py_None);
    return Py_None;
}

static PyMethodDef SharedMemoryMethods[] = {
    {"CreateSharedMemory", PySharedMemory_Create, METH_VARARGS, "Creates Shared Memory"},
    {"OpenSharedMemory", PySharedMemory_Open, METH_VARARGS, "Opens Shared Memory"},
    {"ReadSharedMemory", PySharedMemory_Read, METH_VARARGS, "Reads Shared Memory"},
    {"WriteSharedMemory", PySharedMemory_Write, METH_VARARGS, "Writes Shared Memory"},
    {"UnmapSharedView", PySharedMemory_UnmapView, METH_VARARGS, "Unmaps A Shared View"},
    {"CloseSharedMemory", PySharedMemory_Close, METH_VARARGS, "Close Shared Memory"},
    {NULL, NULL, 0, NULL},
};

static struct PyModuleDef SharedMemoryModule = {
    PyModuleDef_HEAD_INIT,
    "SharedMemory",
    NULL,
    -1,
    SharedMemoryMethods
};

PyMODINIT_FUNC PyInit_SharedMemory(void) {
    PyObject* pyModule = PyModule_Create(&SharedMemoryModule);
    if (pyModule == NULL) {
        return NULL;
    }

    sharedMemoryError = PyErr_NewException("SharedMemory.SharedMemoryError", NULL, NULL);
    Py_XINCREF(sharedMemoryError);
    if (PyModule_AddObject(pyModule, "SharedMemoryError", sharedMemoryError) < 0) {
        Py_XDECREF(sharedMemoryError);
        Py_CLEAR(sharedMemoryError);
        Py_DECREF(pyModule);
        return NULL;
    }
    return pyModule;
}