from flask import Flask, render_template, send_from_directory, abort
from flask import jsonify
from flaskwebgui import FlaskUI
from flask import redirect
from flask import url_for
from flask import request
from flask import jsonify
from flask import make_response
import glob
import sys

sys.path.append("projBE_modules")
mm = __import__("module_managment", fromlist=[''])

app = Flask(__name__)

ui = FlaskUI(app)

def functest(args):
        return "test"

func_list = {"testfunc": functest}
module_ms = mm.ModuleMS()

module_ms.module_instantiate(func_list)

conn_count = 0
@app.route("/")
def index():
	global conn_count
	ip = request.environ.get('HTTP_X_REAL_IP', request.remote_addr)	
	localhost = "127.0.0.1"
	if ip == localhost:
		conn_count += 1
	
	if ip != localhost or conn_count > 1:
		abort(403)
		
	if ip == localhost:
		return render_template('index.html')

	return render_template('index.html')

@app.route("/fail")
def fail():
	return render_template('fail_index.html')

@app.route("/settings", methods=["POST","GET"])
def Settings():
	return render_template('settings.html')


@app.route("/recv_packet", methods=["POST","GET"])
def recv_packet():
	res = make_response(jsonify({"sdata": "null"}), 200)
	req = request.get_json()
	preq = req.get("request")
	sdata_proto = req.get("sdata")
	#"get-module"
	#module
	#module_request
	if preq == "test":
		res = make_response(jsonify({"sdata": "test"}), 200)

	if preq == "get-module":
		print("getting_module")
		module_res = module_ms.run(req.get("module"), req, req.get("module_request"), sdata_proto)
		print(module_res)
		res = make_response(jsonify(module_res) , 200)

	return res


ui.run()