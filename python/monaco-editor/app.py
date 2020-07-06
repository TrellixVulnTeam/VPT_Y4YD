from flask import Flask, render_template, send_from_directory
from flaskwebgui import FlaskUI

app = Flask(__name__)

ui = FlaskUI(app)

@app.route("/")
def index():
    return render_template('index.html')

ui.run()
