from flask import request
from flask import jsonify
import importlib
import sys
import abc

print("Module class included")

class Module(metaclass=abc.ABCMeta):
	def __init__(self, id, functions):
		self.id = id
		self.runnable = False
		self.functions = functions

	@abc.abstractmethod
	def run(self, req, module_request, sdata_protocol):
		pass

	def SetRunnable(self, runnable):
		self.runnable = runnable

	def GetRunnable(self):
		return self.runnable

	def RUN(self, req, module_request, sdata_protocol):
		if self.runnable == True:
			return self.run(req, module_request, sdata_protocol)
		else:
			return "Module is not runnable"

	def CallBackEndFunction(self, function, args):
		return self.functions[function](args)

	def Get(self):
		return self


class Test_Module(Module):
	def __init__(self, id, functions):
		super().__init__(id, functions)
		self.runnable = True
		self.functions = functions

	def run(self, req, module_request, sdata_protocol):
		return self.id

def testfunc(args):
	return "ree"

function_list = {"testfunc": testfunc}
test_module = Test_Module("Test", function_list)

