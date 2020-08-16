import module

class VPTModule(module.Module):
	def __init__(self, id, functions):
		super().__init__(id, functions)
		self.runnable = True

	def run(self, req, module_request, sdata_protocol):
		return jsonify({"sdata": self.id})

