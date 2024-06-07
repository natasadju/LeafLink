from flask import Flask, request, jsonify, abort
import subprocess
import hmac
import hashlib
import logging

app = Flask(__name__)

app.logger.setLevel(logging.INFO)

webhook_secret = "secret"  # Replace "your_secret_here" with your actual secret

@app.route('/github-action-webhook', methods=['POST'])
def webhook():
    data = request.json
    app.logger.info("Received webhook data: %s", data)

    provided_signature = request.headers.get('X-Hub-Signature-256')
    if not provided_signature:
        abort(400, 'Signature missing')

    # Calculate the expected signature
    expected_signature = 'sha256=' + hmac.new(webhook_secret.encode(), request.data, hashlib.sha256).hexdigest()
    encoded_provided_signature = 'sha256=' + hmac.new(provided_signature.encode(), request.data, hashlib.sha256).hexdigest()

    app.logger.info("Received signature: %s", provided_signature)
    app.logger.info("Encoded signature: %s", encoded_provided_signature)
    app.logger.info("Expected signature: %s", expected_signature)

    if not hmac.compare_digest(expected_signature, encoded_provided_signature):
        abort(403, 'Invalid signature')

    try:
        # Run the update container script
        subprocess.run(['sh', 'update_container.sh'], check=True)
        return jsonify({"message": "Action is done!"})
    except subprocess.CalledProcessError as e:
        abort(500, f'Error updating container: {e}')

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)

