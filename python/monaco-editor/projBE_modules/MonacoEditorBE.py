import module

class MEditor(module.Module):
	def __init__(self, id, functions):
		super().__init__(id, functions)
		self.runnable = True
		self.functions = functions

	def run(self, req, module_request, sdata_protocol):
		return {"sdata": self.id}