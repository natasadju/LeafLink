import sys
import os
import torch
from PIL import Image
from torchvision.transforms import functional as F
from torchvision.models.detection import ssdlite320_mobilenet_v3_large
from torchvision.models.detection.ssdlite import SSDLite320_MobileNet_V3_Large_Weights
import json
import numpy as np

def load_model(model_path, num_classes):
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    model = ssdlite320_mobilenet_v3_large(weights=SSDLite320_MobileNet_V3_Large_Weights.COCO_V1)
    model.to(device)

    model.head.classification_head.num_classes = num_classes
    model.load_state_dict(torch.load(model_path, map_location=device))
    model.eval()
    return model, device

def predict_trash_type(image_path, model, device, categories):
    try:
        img = Image.open(image_path).convert("RGB")
        img_tensor = F.to_tensor(img).unsqueeze(0).to(device)

        with torch.no_grad():
            outputs = model(img_tensor)

        scores = outputs[0]["scores"].cpu().numpy()
        labels = outputs[0]["labels"].cpu().numpy()

        confidence_threshold = 0.5
        results = [
            (categories.get(labels[i], "Unknown"), scores[i])
            for i in range(len(scores)) if scores[i] > confidence_threshold
        ]

        return results if results else "No confident predictions."
    except Exception as e:
        return f"Error processing image: {str(e)}"

if __name__ == "__main__":
    # Dynamically determine the directory of the script
    SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))

    # Use absolute paths for model and annotation file
    MODEL_PATH = os.path.join(SCRIPT_DIR, "garbage_classifier2.pth")
    ANNOTATION_FILE = os.path.join(SCRIPT_DIR, "_annotations.coco.json")

    # Ensure annotation file exists
    if not os.path.exists(ANNOTATION_FILE):
        print(f"Error: Annotation file '{ANNOTATION_FILE}' not found.")
        sys.exit(1)

    with open(ANNOTATION_FILE) as f:
        coco_data = json.load(f)
    categories = {cat["id"]: cat["name"] for cat in coco_data["categories"]}

    num_classes = len(categories) + 1
    model, device = load_model(MODEL_PATH, num_classes)

    if len(sys.argv) < 2:
        print("Usage: python model.py <image_path>")
        sys.exit(1)

    image_path = sys.argv[1]

    if not os.path.exists(image_path):
        print(f"Error: File '{image_path}' not found.")
        sys.exit(1)

    result = predict_trash_type(image_path, model, device, categories)
    print(result)
