from flask import Flask, render_template, send_from_directory
from flaskwebgui import FlaskUI

app = Flask(__name__)

ui = FlaskUI(app)

@app.route("/")
def index():
    return render_template('index.html')

@app.route("/monaco-editor/<path:path>")
def send_monaco_editor_code(path):
    return send_from_directory("monaco-editor", path)

ui.run()
