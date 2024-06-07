# webhook_server.py
from flask import Flask, request

app = Flask(__name__)

@app.route('/webhook', methods=['POST'])
def webhook():
    if request.method == 'POST':
        # Pokličite skripto za posodobitev Docker containerja
        import subprocess
        subprocess.call(['./test.sh'])
        return '', 200
    else:
        return '', 400

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
