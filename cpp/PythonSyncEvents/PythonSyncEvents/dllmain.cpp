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

using namespace std;

map<string, HANDLE> eventHandles;
map<string, HANDLE> mutexHandles;

PyObject* synchronizationError;

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

static PyObject* PySyncEvents_CreateEvent(PyObject* self, PyObject* args) {
    const char* name;
    BOOL manualReset = TRUE, initialState = FALSE;
    if (!PyArg_ParseTuple(args, "s|pp", &name, &manualReset, &initialState)) {
        return NULL;
    }
    HANDLE eventHandle = CreateEventA(NULL, manualReset, initialState, name);
    if (eventHandle == NULL) {
        PyErr_SetString(synchronizationError, GetLastErrorExport());
        return NULL;
    }
    string key;
    do {
        key = randomString();
    } while (eventHandles.count(key) != 0);
    eventHandles.insert(make_pair(key, eventHandle));
    return PyUnicode_FromString(key.c_str());
}

static PyObject* PySyncEvents_CreateMutex(PyObject* self, PyObject* args) {
    const char* name;
    BOOL isOwned = FALSE;
    if (!PyArg_ParseTuple(args, "s|p", &name, &isOwned)) {
        return NULL;
    }
    HANDLE mutexHandle = CreateMutexA(NULL, isOwned, name);
    if (mutexHandle == NULL) {
        PyErr_SetString(synchronizationError, GetLastErrorExport());
        return NULL;
    }
    string key;
    do {
        key = randomString();
    } while (mutexHandles.count(key) != 0);
    mutexHandles.insert(make_pair(key, mutexHandle));
    return PyUnicode_FromString(key.c_str());
}

static PyObject* PySyncEvents_OpenEvent(PyObject* self, PyObject* args) {
    const char* name;
    if (!PyArg_ParseTuple(args, "s", &name)) {
        return NULL;
    }
    HANDLE eventHandle = OpenEventA(EVENT_ALL_ACCESS, FALSE, name);
    if (eventHandle == NULL) {
        PyErr_SetString(synchronizationError, GetLastErrorExport());
        return NULL;
    }
    string key;
    do {
        key = randomString();
    } while (eventHandles.count(key) != 0);
    eventHandles.insert(make_pair(key, eventHandle));
    return PyUnicode_FromString(key.c_str());
}

static PyObject* PySyncEvents_OpenMutex(PyObject* self, PyObject* args) {
    const char* name;
    if (!PyArg_ParseTuple(args, "s", &name)) {
        return NULL;
    }
    HANDLE mutexHandle = OpenMutexA(MUTEX_ALL_ACCESS, FALSE, name);
    if (mutexHandle == NULL) {
        PyErr_SetString(synchronizationError, GetLastErrorExport());
        return NULL;
    }
    string key;
    do {
        key = randomString();
    } while (mutexHandles.count(key) != 0);
    mutexHandles.insert(make_pair(key, mutexHandle));
    return PyUnicode_FromString(key.c_str());
}

static PyObject* PySyncEvents_SetEvent(PyObject* self, PyObject* args) {
    const char* handle;
    if (!PyArg_ParseTuple(args, "s", &handle)) {
        return NULL;
    }
    if (eventHandles.count(string(handle)) == 0) {
        PyErr_SetString(synchronizationError, "Handle does not exist");
        return NULL;
    }
    if (!SetEvent(eventHandles.find(string(handle))->second)) {
        PyErr_SetString(synchronizationError, GetLastErrorExport());
        return NULL;
    }
    Py_INCREF(Py_None);
    return Py_None;
}

static PyObject* PySyncEvents_ResetEvent(PyObject* self, PyObject* args) {
    const char* handle;
    if (!PyArg_ParseTuple(args, "s", &handle)) {
        return NULL;
    }
    if (eventHandles.count(string(handle)) == 0) {
        PyErr_SetString(synchronizationError, "Handle does not exist");
        return NULL;
    }
    if (!ResetEvent(eventHandles.find(string(handle))->second)) {
        PyErr_SetString(synchronizationError, GetLastErrorExport());
        return NULL;
    }
    Py_INCREF(Py_None);
    return Py_None;
}

static PyObject* PySyncEvents_WaitForEvent(PyObject* self, PyObject* args) {
    const char* handle;
    DWORD timeout = INFINITE;
    if (!PyArg_ParseTuple(args, "s|k", &handle, &timeout)) {
        return NULL;
    }
    if (eventHandles.count(string(handle)) == 0) {
        PyErr_SetString(synchronizationError, "Handle does not exist");
        return NULL;
    }
    DWORD result;
    if ((result = WaitForSingleObject(eventHandles.find(string(handle))->second, timeout)) == WAIT_FAILED) {
        PyErr_SetString(synchronizationError, GetLastErrorExport());
        return NULL;
    }
    return PyLong_FromUnsignedLong(result);
}

static PyObject* PySyncEvents_CloseEvent(PyObject* self, PyObject* args) {
    const char* handle;
    if (!PyArg_ParseTuple(args, "s", &handle)) {
        return NULL;
    }
    if (eventHandles.count(string(handle)) == 0) {
        PyErr_SetString(synchronizationError, "Handle does not exist");
        return NULL;
    }
    if (!CloseHandle(eventHandles.find(string(handle))->second)) {
        PyErr_SetString(synchronizationError, GetLastErrorExport());
        return NULL;
    }
    Py_INCREF(Py_None);
    return Py_None;
}

static PyObject* PySyncEvents_WaitForMutex(PyObject* self, PyObject* args) {
    const char* handle;
    DWORD timeout = INFINITE;
    if (!PyArg_ParseTuple(args, "s|k", &handle, &timeout)) {
        return NULL;
    }
    if (mutexHandles.count(string(handle)) == 0) {
        PyErr_SetString(synchronizationError, "Handle does not exist");
        return NULL;
    }
    DWORD result;
    if ((result = WaitForSingleObject(mutexHandles.find(string(handle))->second, timeout)) == WAIT_FAILED) {
        PyErr_SetString(synchronizationError, GetLastErrorExport());
        return NULL;
    }
    return PyLong_FromUnsignedLong(result);
}

static PyObject* PySyncEvents_ReleaseMutex(PyObject* self, PyObject* args) {
    const char* handle;
    if (!PyArg_ParseTuple(args, "s", &handle)) {
        return NULL;
    }
    if (mutexHandles.count(string(handle)) == 0) {
        PyErr_SetString(synchronizationError, "Handle does not exist");
        return NULL;
    }
    if (!ReleaseMutex(mutexHandles.find(string(handle))->second)) {
        PyErr_SetString(synchronizationError, GetLastErrorExport());
        return NULL;
    }
    Py_INCREF(Py_None);
    return Py_None;
}

static PyObject* PySyncEvents_CloseMutex(PyObject* self, PyObject* args) {
    const char* handle;
    if (!PyArg_ParseTuple(args, "s", &handle)) {
        return NULL;
    }
    if (mutexHandles.count(string(handle)) == 0) {
        PyErr_SetString(synchronizationError, "Handle does not exist");
        return NULL;
    }
    if (!CloseHandle(mutexHandles.find(string(handle))->second)) {
        PyErr_SetString(synchronizationError, GetLastErrorExport());
        return NULL;
    }
    Py_INCREF(Py_None);
    return Py_None;
}

static PyMethodDef SyncEventsMethods[] = {
    {"CreateEvent", PySyncEvents_CreateEvent, METH_VARARGS, "Creates an Event"},
    {"CreateMutex", PySyncEvents_CreateMutex, METH_VARARGS, "Creates a Mutex"},
    {"OpenEvent", PySyncEvents_OpenEvent, METH_VARARGS, "Opens an Event"},
    {"OpenMutex", PySyncEvents_OpenMutex, METH_VARARGS, "Opens a Mutex"},
    {"SetEvent", PySyncEvents_SetEvent, METH_VARARGS, "Signals an Event"},
    {"ResetEvent", PySyncEvents_ResetEvent, METH_VARARGS, "Resets an Event"},
    {"WaitForEvent", PySyncEvents_WaitForEvent, METH_VARARGS, "Waits for an Event to be Signaled"},
    {"CloseEvent", PySyncEvents_CloseEvent, METH_VARARGS, "Closes an Event"},
    {"WaitForMutex", PySyncEvents_WaitForMutex, METH_VARARGS, "Aquires for a Mutex"},
    {"ReleaseMutex", PySyncEvents_ReleaseMutex, METH_VARARGS, "Releases a Mutex"},
    {"CloseMutex", PySyncEvents_CloseMutex, METH_VARARGS, "Closes a Mutex"},
    {NULL, NULL, 0, NULL},
};

static struct PyModuleDef SyncEventsModule = {
    PyModuleDef_HEAD_INIT,
    "SyncEvents",
    NULL,
    -1,
    SyncEventsMethods
};

PyMODINIT_FUNC PyInit_SyncEvents(void) {
    PyObject* pyModule = PyModule_Create(&SyncEventsModule);
    if (pyModule == NULL) {
        return NULL;
    }

    synchronizationError = PyErr_NewException("SyncEvents.SyncError", NULL, NULL);
    Py_XINCREF(synchronizationError);
    if (PyModule_AddObject(pyModule, "SyncError", synchronizationError) < 0) {
        Py_XDECREF(synchronizationError);
        Py_CLEAR(synchronizationError);
        Py_DECREF(pyModule);
        return NULL;
    }
    return pyModule;
}