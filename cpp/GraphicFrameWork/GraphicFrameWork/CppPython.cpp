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
	//string relativeFilename = pythonDir + filename + ".py";
	const char* filenameAsString = filename.c_str();
	Py_Initialize();
	PyObject* syspath = PySys_GetObject("path");
	string pythonpath = pythonDir + "Python\\Python36";
	PyList_Append(syspath, PyBytes_FromString(pythonpath.c_str()));
	string codepath = pythonDir + "monaco-editor";
	PyList_Append(syspath, PyBytes_FromString(codepath.c_str()));
	RegisterCallbacks(callbacks);
	PyObject* moduleName = PyBytes_FromString(filenameAsString);
	PyObject* pythonModule = PyImport_Import(moduleName);
	Py_DECREF(moduleName);
	PyObject* mainMethod = PyObject_GetAttrString(pythonModule, "main");
	PyObject* result = PyObject_CallObject(mainMethod, getArgs());
	Py_DECREF(mainMethod);
	resultHandler(result);
	if (result != NULL) {
		Py_DECREF(result);
	}
	Py_DECREF(pythonModule);
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