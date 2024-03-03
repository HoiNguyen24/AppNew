package src.func;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Resize {

    private static final int IMG_WIDTH = 300;
    private static final int IMG_HEIGHT = 150;

    public static void main(String[] args) throws IOException {

        Path source = Paths.get("C:\\test\\google.png");
        Path target = Paths.get("C:\\test\\resize.png");

        try (InputStream is = new FileInputStream(source.toFile())) {
            resize(is, target, IMG_WIDTH, IMG_HEIGHT);
        }

    }

    public static void resize(InputStream input, Path target,
                               int width, int height) throws IOException {

        BufferedImage originalImage = ImageIO.read(input);

        /**
         * SCALE_AREA_AVERAGING
         * SCALE_DEFAULT
         * SCALE_FAST
         * SCALE_REPLICATE
         * SCALE_SMOOTH
         */
        Image newResizedImage = originalImage
                .getScaledInstance(width, height, Image.SCALE_SMOOTH);

        String s = target.getFileName().toString();
        String fileExtension = s.substring(s.lastIndexOf(".") + 1);

        // we want image in png format
        ImageIO.write(convertToBufferedImage(newResizedImage),
                fileExtension, target.toFile());

    }

    public static BufferedImage convertToBufferedImage(Image img) {

        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bi = new BufferedImage(
                img.getWidth(null), img.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics2D = bi.createGraphics();
        graphics2D.drawImage(img, 0, 0, null);
        graphics2D.dispose();

        return bi;
    }

}
