#include "CppPython.h"

static mutex execLock;
PyModuleDef CppPython::currentCallbackModule = PyModuleDef{ NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL };

void CppPython::ExecPython(string filename, function<PyObject*()> getArgs, vector<PyMethodDef> callbacks, function<void(PyObject*)> resultHandler) {
	lock_guard<mutex> execLG(execLock);
	string cmd = "\"" + pythonDir + "Python/Python36/python.exe\" \"" + pythonDir + filename + ".py\"";
	system(cmd.c_str());
	/*string pythonExecDir = pythonDir + "Python\\Python36\\python-3.6.0-embed-amd64.zip";
	string relativeFilename = pythonDir + filename;
	const char* filenameAsString = relativeFilename.c_str();*/

	/*RegisterCallbacks(callbacks);
	Py_SetProgramName(Utils::UnConstWChar(Utils::FromString(relativeFilename).c_str()));
	Py_Initialize();
	Py_SetPath(Utils::FromString(pythonExecDir).c_str());*/
	/*PyRun_SimpleString("import sys");
	string addDirCode = "sys.path.append('" + pythonDir + "')";
	PyRun_SimpleString(addDirCode.c_str());*/

	//PyObject* moduleName = PyBytes_FromString(filenameAsString);
	////cout << PyUnicode_AsUTF8(moduleName) << endl;
	//PyObject* pythonModule = PyImport_AddModule(filenameAsString);
	//Py_DECREF(moduleName);
	//PyObject* mainMethod = PyObject_GetAttrString(pythonModule, "main");
	//PyObject* result = PyObject_CallObject(mainMethod, getArgs());
	//Py_DECREF(mainMethod);
	//resultHandler(result);
	//if (result != NULL) {
	//	Py_DECREF(result);
	//}
	//Py_DECREF(pythonModule);
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