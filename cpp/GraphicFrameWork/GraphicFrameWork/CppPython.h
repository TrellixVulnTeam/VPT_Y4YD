#pragma once
#include "IPython.h"
#include <string>
#include <vector>
#include "projects/VPT/RelativePaths.h"
#include <functional>
#include <mutex>

using namespace std;

struct PythonCallback {
	string name;
	string format;
	string description;
	function < PyObject* (PyObject* self, PyObject* args) > callback;
	int flags = METH_VARARGS;
};

class CppPython {
public:
	static void ExecPython(string filename, vector<PythonCallback> callbacks = vector<PythonCallback>(), function<void(PyObject*)> resultHandler = [](PyObject* result) {});
	static void RegisterCallbacks(vector<PythonCallback> callbacks);
};

extern "C" {
	class CustomPythonModule {
	public:
		CustomPythonModule(PyModuleDef def) : def(def) {}
		__declspec(dllexport) PyObject* Init(void) { return PyModule_Create(&def); }
		PyModuleDef def;
	};
}