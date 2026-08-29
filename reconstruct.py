import os
import glob
import base64

def main():
    print("Reconstructing cmfi-release.aab from text parts...")
    base_dir = os.path.dirname(os.path.abspath(__file__))
    parts_dir = os.path.join(base_dir, "aab_parts")
    parts = sorted(glob.glob(os.path.join(parts_dir, "part_*.txt")))
    
    if not parts:
        print("Error: No part_*.txt files found in aab_parts folder!")
        return

    b64_data = ""
    for p in parts:
        print(f"Reading {os.path.basename(p)}...")
        with open(p, "r", encoding="utf-8") as f:
            b64_data += f.read()

    print("Decoding base64 data into binary AAB...")
    binary_data = base64.b64decode(b64_data)
    
    out_path = os.path.join(base_dir, "cmfi-release.aab")
    with open(out_path, "wb") as f:
        f.write(binary_data)

    size_mb = len(binary_data) / (1024 * 1024)
    print(f"\nSUCCESS! Created 'cmfi-release.aab' ({size_mb:.2f} MB).")
    print("You can now upload 'cmfi-release.aab' directly to Google Play Console!")

if __name__ == "__main__":
    main()
