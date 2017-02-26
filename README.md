# Steganography in Java

## Intrudoction
- It's a technology which can hide some information into an image or a video
- I know it when I read the *Modern Operation System (3e)*. It's in the section 9.3.8 The hidden channel
- I just test this program on the **PNG** images successfully. However, it can't work on JPG images because of some special format rules.

## QuickStart
- Just run the `run.sh`!

## Usage
- Compile the `Steganography.java` first
- `java Steganography image.png message.txt`: Hide the text in message.txt into the image.png and generate a new image called resultImage.png
- `java Steganography imageWithHiddenMessage.png`: Extract the hidden message in the specific image to a text file called hiddenContent.txt
