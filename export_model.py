"""
Model Export Utility for CrowdShield Vision Engine.
Exports YOLO PyTorch models to TensorRT (.engine) or ONNX (.onnx) for accelerated GPU inference.
"""

import sys
import os
import torch
from ultralytics import YOLO


def export_model(
    model_name: str = "yolov8n.pt",
    target_format: str = "engine",  # "engine" for TensorRT, "onnx" for ONNX
    device: str = "cuda" if torch.cuda.is_available() else "cpu",
    half_precision: bool = True,
    img_size: int = 640,
):
    """
    Exports YOLO model to TensorRT (.engine) or ONNX format.
    """
    print(f"[*] Loading base model: {model_name}...")
    model = YOLO(model_name)

    print(f"[*] Exporting to format='{target_format}' on device='{device}' (half={half_precision})...")
    try:
        exported_path = model.export(
            format=target_format,
            device=device,
            half=half_precision,
            imgsz=img_size,
            dynamic=False,
        )
        print(f"[+] Successfully exported model to: {exported_path}")
        return exported_path
    except Exception as e:
        print(f"[-] Export to {target_format} encountered an issue: {e}")
        if target_format == "engine":
            print("[*] Falling back to ONNX export for cross-platform GPU/CPU acceleration...")
            onnx_path = model.export(format="onnx", imgsz=img_size, half=False)
            print(f"[+] Successfully exported ONNX model to: {onnx_path}")
            return onnx_path
        raise e


if __name__ == "__main__":
    fmt = sys.argv[1] if len(sys.argv) > 1 else "engine"
    dev = "cuda" if torch.cuda.is_available() else "0"
    export_model(target_format=fmt, device=dev)
