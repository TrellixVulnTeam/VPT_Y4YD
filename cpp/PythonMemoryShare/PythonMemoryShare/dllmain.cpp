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

using namespace std;

map<string, HANDLE> fileHandles;

PyObject* sharedMemoryError;

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

static string randomString() {
    char* randChars = new char[32];
    gen_random(randChars, 32);
    return string(randChars);
}

static PyObject* PySharedMemory_Create(PyObject* self, PyObject* args) {
    const char* name;
    size_t bufSize;
    if (!PyArg_ParseTuple(args, "s", &name, &bufSize)) {
        return NULL;
    }
    WCHAR* nameW = new WCHAR[sizeof(name) / sizeof(char)];
    mbstowcs(nameW, name, sizeof(name) / sizeof(char));
HANDLE memoryHandle = CreateFileMapping(INVALID_HANDLE_VALUE, NULL, PAGE_READWRITE, 0, bufSize, nameW);
string key = randomString();
fileHandles.insert(make_pair(key, memoryHandle));
return PyUnicode_FromString(key.c_str());
}

static PyObject* PySharedMemory_Open(PyObject* self, PyObject* args) {
    const char* name;
    if (!PyArg_ParseTuple(args, "s", &name)) {
        return NULL;
    }
    HANDLE memoryHandle = OpenFileMappingA(FILE_MAP_ALL_ACCESS, FALSE, name);
    string key = randomString();
    fileHandles.insert(make_pair(key, memoryHandle));
    return PyUnicode_FromString(key.c_str());
}

static PyObject* PySharedMemory_Read(PyObject* self, PyObject* args) {
    const char* handleHandle;
    size_t bytesToRead;
    if (!PyArg_ParseTuple(args, "sI", &handleHandle, &bytesToRead)) {
        return NULL;
    }
    if (fileHandles.count(string(handleHandle)) != 1) {
        PyErr_SetString(sharedMemoryError, "Handle Does Not Exist");
        return NULL;
    }
    LPTSTR data = (LPTSTR)MapViewOfFile(fileHandles.find(string(handleHandle))->second, FILE_MAP_ALL_ACCESS, 0, 0, bytesToRead);
    if (data == NULL) {
        PyErr_SetString(sharedMemoryError, "Error Occured: " + GetLastError());
        return NULL;
    }
    UnmapViewOfFile(data);
    return PyUnicode_FromString(_bstr_t(data));
}

static PyObject* PySharedMemory_Write(PyObject* self, PyObject* args) {
    const char* handleHandle;
    size_t bytesToRead;
    const char* text;
    if (!PyArg_ParseTuple(args, "sIs", &handleHandle, &bytesToRead, &text)) {
        return NULL;
    }
    if (fileHandles.count(string(handleHandle)) != 1) {
        PyErr_SetString(sharedMemoryError, "Handle Does Not Exist");
        return NULL;
    }
    LPTSTR data = (LPTSTR)MapViewOfFile(fileHandles.find(string(handleHandle))->second, FILE_MAP_ALL_ACCESS, 0, 0, bytesToRead);
    if (data == NULL) {
        PyErr_SetString(sharedMemoryError, "Error Occured: " + GetLastError());
        return NULL;
    }
    TCHAR* msg = new TCHAR[sizeof(text) / sizeof(char)];
    mbstowcs(msg, text, sizeof(text) / sizeof(char));
    CopyMemory((PVOID)data, text, sizeof(text));
    UnmapViewOfFile(data);
    Py_IncRef(Py_None);
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
    Py_IncRef(Py_None);
    return Py_None;
}

static PyMethodDef SharedMemoryMethods[] = {
    {"CreateSharedMemory", PySharedMemory_Create, METH_VARARGS, "Creates Shared Memory"},
    {"OpenSharedMemory", PySharedMemory_Open, METH_VARARGS, "Opens Shared Memory"},
    {"ReadSharedMemory", PySharedMemory_Read, METH_VARARGS, "Reads Shared Memory"},
    {"WriteSharedMemory", PySharedMemory_Write, METH_VARARGS, "Writes Shared Memory"},
    {"CloseSharedMemory", PySharedMemory_Close, METH_VARARGS, "Close Shared Memory"},
    {NULL, NULL, 0, NULL},
};

static struct PyModuleDef SharedMemoryModule = {
    PyModuleDef_HEAD_INIT,
    "sharedmemory",
    NULL,
    -1,
    SharedMemoryMethods
};

PyMODINIT_FUNC PyInit_sharedmemory(void) {
    PyObject* pyModule = PyModule_Create(&SharedMemoryModule);
    if (pyModule == NULL) {
        return NULL;
    }

    sharedMemoryError = PyErr_NewException("sharedmemory.error", NULL, NULL);
    Py_XINCREF(sharedMemoryError);
    if (PyModule_AddObject(pyModule, "error", sharedMemoryError) < 0) {
        Py_XDECREF(sharedMemoryError);
        Py_CLEAR(sharedMemoryError);
        Py_DECREF(pyModule);
        return NULL;
    }
    return pyModule;
}