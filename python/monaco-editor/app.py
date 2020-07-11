from flask import Flask, render_template, send_from_directory, abort
from flask import jsonify
from flaskwebgui import FlaskUI
from flask import redirect
from flask import url_for
from flask import request
from flask import jsonify
from flask import make_response
import glob

app = Flask(__name__)

ui = FlaskUI(app)

class RFA:
	def __init__(self, filetype):
		self.filetype = filetype

	def GetFiles(self):
		return glob.glob("../CodeProcesser/code/" + self.filetype)

conn_count = 0
settings = ["python", "vs-light"]
registered_files = ["*.py", "*.cpp", "*.js"]
@app.route("/", methods=["POST","GET"])
def index():
	global conn_count
	ip = request.environ.get('HTTP_X_REAL_IP', request.remote_addr)	
	localhost = "127.0.0.1"
	if ip == localhost:
		conn_count += 1

	if request.method == "POST":
		conn_count -= 1
		#if request.form["submit_code"] == "submit":
		code = request.form["code"]

		import os
		os.system("..\Python\Python36\python.exe ../CodeProcesser/code_parser.py " + code + " " + settings[0])
		return render_template('index.html')

	
	if ip != localhost or conn_count > 1:
		abort(403)
		
	if ip == localhost:
		return render_template('index.html')

@app.route("/fail")
def fail():
	return render_template('fail_index.html')

@app.route("/settings", methods=["POST","GET"])
def Settings():
	return render_template('settings.html')

@app.route("/change_settings", methods=["POST","GET"])
def Change_Settings():
	global conn_count
	lang = request.form["language"]
	theme = request.form["theme"]
	print(lang)
	settings[0] = lang
	settings[1] = theme
	conn_count -= 1
	return redirect(url_for("index"))

@app.route("/req_settings_packet", methods=["POST", "GET"])
def SendSettings():
	req = request.get_json()
	print(req)
	res = make_response(jsonify({"lang": settings[0]}, {"theme": settings[1]}), 200)
	return res

@app.route("/req_getcodefile_paths",  methods=["POST", "GET"])
def SendCodeFiles():
	req = request.get_json()
	print(req)
	py_files = RFA(registered_files[0])
	print(py_files.GetFiles())
	res = make_response(jsonify({"file": "test"}))
	return res


ui.run()