#include "CppPython.h"

static mutex execLock;
PyModuleDef CppPython::currentCallbackModule = PyModuleDef{ NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL };

void CppPython::ExecPython(string filename, function<PyObject*()> getArgs, vector<PyMethodDef> callbacks, function<void(PyObject*)> resultHandler) {
	lock_guard<mutex> execLG(execLock);
	std::wstring_convert<std::codecvt_utf8_utf16<wchar_t>> converter;
	//string pythonExecDir = pythonDir + "Python\\Python36";
	//wchar_t* pythonExecDirW = new wchar_t[0];
	////memcpy(pythonExecDirW, converter.from_bytes(pythonExecDir).c_str(), pythonExecDir.size() * (sizeof(wchar_t)));
	//Py_SetPythonHome(pythonExecDirW);
	//string define = pythonDir + "monaco-editor";
	//Py_SetPath(converter.from_bytes(define).c_str());
	string relativeFilename = pythonDir + filename;
    // Initialize the Python Interpreter

    PyObject* pName, * pModule, * pDict, * pFunc, * pValue;
    Py_Initialize();

    // Build the name object
    pName = PyUnicode_DecodeFSDefault(relativeFilename.c_str());

    // Load the module object
    pModule = PyImport_Import(pName);

    // pDict is a borrowed reference 
    pDict = PyModule_GetDict(pModule);

    // pFunc is also a borrowed reference 
    pFunc = PyDict_GetItemString(pDict, "main");

    if (PyCallable_Check(pFunc))
    {
        PyObject_CallObject(pFunc, NULL);
    }
    else
    {
        PyErr_Print();
    }

    // Clean up
    Py_DECREF(pModule);
    Py_DECREF(pName);

    // Finish the Python Interpreter
    Py_Finalize();
}

void CppPython::RegisterCallbacks(vector<PyMethodDef> callbacks) {
	if (callbacks.empty()) {
		return;
	}
	size_t numCallbacks = callbacks.size() + 1;
	static PyMethodDef* callbacksDef = new PyMethodDef[numCallbacks];
	callbacksDef[callbacks.size()] = { NULL, NULL, 0, NULL };
	memcpy(callbacksDef, &callbacks[0], callbacks.size() * sizeof(PyMethodDef));
	currentCallbackModule = PyModuleDef{PyModuleDef_HEAD_INIT, "cppCallbacks", NULL, -1, callbacksDef, NULL, NULL, NULL, NULL};
	PyImport_AppendInittab("cppCallbacks", &InitCurrentCallbackModule);
}