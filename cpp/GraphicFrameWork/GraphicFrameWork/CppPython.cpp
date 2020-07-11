#include "CppPython.h"

static mutex execLock;

void CppPython::ExecPython(string filename, function<PyObject*()> getArgs, vector<PyMethodDef> callbacks, function<void(PyObject*)> resultHandler) {
	lock_guard<mutex> execLG(execLock);
	std::wstring_convert<std::codecvt_utf8_utf16<wchar_t>> converter;
	string pythonExecDir = pythonDir + "Python\\Python36";
	Py_SetPath(converter.from_bytes(pythonExecDir).c_str());
	string relativeFilename = pythonDir + filename + ".py";
	const char* filenameAsString = relativeFilename.c_str();
	RegisterCallbacks(callbacks);
	Py_Initialize();
	PyObject* moduleName = PyUnicode_FromString(filenameAsString);
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