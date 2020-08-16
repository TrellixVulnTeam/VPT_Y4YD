from flask import Flask, render_template
from flask import make_response, request
from flask import jsonify
import importlib
import sys
sys.path.append("py_vptclient")
__import__("vpt_client")

sys.path.append("projBE_modules")
__import__("module_managment")

app = Flask(__name__)

ip_table = []
ip_table.append("127.0.0.1")


@app.route("/")
def index():
	return render_template('index.html')

@app.route("/contacts")
def contacts():
	return render_template('contacts.html')

@app.route("/portfolio")
def portfolio():
	return render_template('portfolio.html')

@app.route("/recv_packet", methods=["POST","GET"])
def recv_packet():
	res = make_response(jsonify({"sdata": "null"}), 200)
	req = request.get_json()
	preq = req.get("request")
	sdata_proto = req.get("sdata")
	#"get-module"
	#module
	#module-request
	if preq == "test":
		res = make_response(jsonify({"sdata": "test"}), 200)

	if preq == "ip_table":
		if sdata_proto == "obj_data":
			pass

		if sdata_proto == "data":
			ip_tableindex = req.get("ip_index")
			res = make_response(jsonify({"sdata": ip_table[ip_tableindex]}), 200)

	if preq == "ip_table_len":
		res = make_response(jsonify({"sdata": len(ip_table) - 1}), 200)

	#if preq == "get-module":
		#module_res = module_ms.run(req.get("module"), req, req.get("module-request"), sdata_proto)
		#res = make_response(module_res, 200)

	return res

if __name__ == "__main__":
	app.run(host="0.0.0.0", port=443, ssl_context=('cert.pem', 'key.pem'))


