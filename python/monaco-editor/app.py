from flask import Flask, render_template, send_from_directory
from flask import jsonify
from flaskwebgui import FlaskUI
from flask import redirect
from flask import url_for
from flask import request

app = Flask(__name__)

ui = FlaskUI(app)

conn_count = 0

@app.route("/", methods=["GET"])
def index():
	global conn_count
	ip = request.environ.get('HTTP_X_REAL_IP', request.remote_addr)	
	localhost = "127.0.0.1"
	if ip == localhost:
		conn_count += 1

	if ip != localhost or conn_count > 1:
		return redirect(url_for("fail"))
		
	if ip == localhost:
		print(conn_count) 
		return render_template('index.html')

@app.route("/fail")
def fail():
	return render_template('fail_index.html')	

ui.run()
