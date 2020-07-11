#pragma once
#include "IPython.h"
#include <string>
#include <vector>
#include "projects/VPT/RelativePaths.h"
#include <functional>
#include <mutex>
#include <locale>
#include <codecvt>
#include <string>

using namespace std;

extern "C" {
	class CppPython {
	public:
		static void ExecPython(string filename, function<PyObject*()> getArgs = []() { return (PyObject*)NULL; },
			vector<PyMethodDef> callbacks = vector<PyMethodDef>(), function<void(PyObject*)> resultHandler = [](PyObject* result) {});
		static void RegisterCallbacks(vector<PyMethodDef> callbacks);
		static PyModuleDef currentCallbackModule;
		__declspec(dllexport) static PyObject* InitCurrentCallbackModule(void) { return PyModule_Create(&currentCallbackModule); }
	};

	PyModuleDef CppPython::currentCallbackModule = PyModuleDef{ NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL };
}