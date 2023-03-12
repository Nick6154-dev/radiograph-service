package com.yps.service.impl;

import com.yps.service.RadiographService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.nio.file.Paths;
import java.util.Arrays;

@Service
public class RadiographServiceImpl implements RadiographService {

    @Value("${env.size-image}")
    private Integer imageSize;

    @Value(("${env.model-path}"))
    private String modelPath;

    @Value("${env.input-node-name}")
    private String inputNodeName;

    @Value("${env.output-node-name}")
    private String outputNodeName;

    @Override
    public Integer validateImage(BufferedImage image) {
        float[] imageData = preprocessImage(resizeImage(image));
        Tensor<Float> imageTensor = Tensor.create(new long[]{1, imageSize, imageSize, 3}, FloatBuffer.wrap(imageData));
        SavedModelBundle bundle = SavedModelBundle.load(Paths.get(modelPath).toString(),"serve");
        Session session = bundle.session();
        Tensor<?> resultTensor = session.runner().feed(inputNodeName, imageTensor).fetch(outputNodeName).run().get(0);
        session.close();
        float[][] resultData = new float[1][2];
        resultTensor.copyTo(resultData);
        return argmax(resultData[0]);
    }

    private float[] preprocessImage(BufferedImage image) {
        float[] imageData = new float[imageSize * imageSize * 3];
        int[] pixels = image.getRGB(
                0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth()
        );
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            imageData[i * 3] = ((pixel >> 16) & 0xff) / 255.0f;
            imageData[i * 3 + 1] = ((pixel >> 8) & 0xff) / 255.0f;
            imageData[i * 3 + 2] = (pixel & 0xff) / 255.0f;
        }
        imageData = Arrays.copyOf(imageData, imageSize * imageSize * 3);
        return imageData;
    }

    private int argmax(float[] array) {
        int argmax = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[argmax]) {
                argmax = i;
            }
        }
        return argmax;
    }

    private BufferedImage resizeImage(BufferedImage image) {
        Image scaledImage = image.getScaledInstance(imageSize, imageSize, Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();
        return resizedImage;
    }

}
