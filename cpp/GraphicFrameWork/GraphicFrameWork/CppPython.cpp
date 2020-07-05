#include "CppPython.h"

static mutex execLock;

void CppPython::ExecPython(string filename, vector<PythonCallback> callbacks, function<void(PyObject*)> resultHandler) {
	lock_guard<mutex> execLG(execLock);
	string relativeFilename = pythonDir + filename + ".py";
	const char* filenameAsString = relativeFilename.c_str();
	RegisterCallbacks(callbacks);
	Py_Initialize();
	PyObject* moduleName = PyUnicode_FromString(filenameAsString);
	PyObject* pythonModule = PyImport_Import(moduleName);
	Py_DECREF(moduleName);
	PyObject* mainMethod = PyObject_GetAttrString(pythonModule, "main");
	PyObject* result = PyObject_CallObject(mainMethod, NULL);
	Py_DECREF(mainMethod);
	resultHandler(result);
	if (result != NULL) {
		Py_DECREF(result);
	}
	Py_DECREF(pythonModule);
	Py_Finalize();
}

void CppPython::RegisterCallbacks(vector<PythonCallback> callbacks) {
	if (callbacks.empty()) {
		return;
	}
	size_t numCallbacks = callbacks.size() + 1;
	static PyMethodDef* callbacksDef = new PyMethodDef[numCallbacks];
	callbacksDef[callbacks.size()] = { NULL, NULL, 0, NULL };
	for (int i = 0; i < callbacks.size(); i++) {
		PythonCallback callback = callbacks[i];
		callbacksDef[i] = {callback.name.c_str(), callback.callback.target, callback.flags, callback.description.c_str()};
	}
	PyModuleDef callbackModuleDef = {PyModuleDef_HEAD_INIT, "cppCallbacks", NULL, -1, callbacksDef, NULL, NULL, NULL, NULL};
	static CustomPythonModule callbackModule(callbackModuleDef);
	PyImport_AppendInittab("cppCallbacks", &callbackModule.Init);
}