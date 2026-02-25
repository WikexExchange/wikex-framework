package com.wikex.wikex.admin.controller.system;

import com.wikex.wikex.controller.BaseController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Api(tags = "Captcha")
@Controller
public class CaptchaController extends BaseController {

    @Autowired
    private RedisTemplate redisTemplate;

    // Character source
    private String text = "0123456789abcdefghijklmnopqrstuvwxyz";
    private int length = 4;
    private int width = 200;
    private int height = 64;
    private Font font = new Font("Arial", Font.ITALIC | Font.BOLD, (int)(height * 0.8));
    private boolean crossLine = false;
    private boolean twistImage = true;

    public String getText() {
        return text;
    }

    public int getLength() {
        return length;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Generate captcha image request
     * @throws IOException
     */
    @ApiOperation(value = "Request to generate captcha image")
    @RequestMapping("/captcha")
    public void genCaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Create image in memory
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // Get graphics context
        Graphics g = image.getGraphics();
        // Random generator
        Random random = new Random();
        // Set background color
        g.setColor(new Color(255, 255, 255));
        g.fillRect(0, 0, width, height);
        // Set font
        g.setFont(font);
        // Generate captcha string
        String sRand = "";
        // Text X coordinate
        int codeX = width / (length + 1);
        for (int i = 0; i < length; i++) {
            String rand = String.valueOf(getRandomText());
            sRand += rand;
            // Draw captcha on image
            g.setColor(getRandColor(100, 150));
            g.drawString(rand, codeX * i + 10, height - 20);
        }
        // Draw interference lines
        if (crossLine) {
            for (int i = 0; i < (random.nextInt(5) + 5); i++) {
                g.setColor(new Color(random.nextInt(255) + 1, random.nextInt(255) + 1, random.nextInt(255) + 1));
                g.drawLine(random.nextInt(width), random.nextInt(height),
                        random.nextInt(width), random.nextInt(height));
            }
        }
        // Distort image
        if (twistImage) {
            image = twistImage(image);
        }

        // Save captcha into Redis with page key
        String pageId = request(request, "cid");
        String sid = request(request, "sid");
        String key = "CAPTCHA_" + pageId + sid;
        ValueOperations<String, String> opt = redisTemplate.opsForValue();
        opt.set(key, sRand);
        redisTemplate.expire(key, 10, TimeUnit.MINUTES);

        // Make image effective
        g.dispose();
        ServletOutputStream responseOutputStream = response.getOutputStream();
        // Output image to page
        ImageIO.write(image, "JPEG", responseOutputStream);

        // Disable caching
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        responseOutputStream.flush();
        responseOutputStream.close();
    }

    /**
     * Apply sine wave distortion to the image
     * @return BufferedImage
     */
    private BufferedImage twistImage(BufferedImage buffImg) {
        Random random = new Random();
        double dMultValue = random.nextInt(10) + 5; // Wave amplitude multiplier, higher means stronger distortion
        double dPhase = random.nextInt(6); // Wave starting phase
        BufferedImage destBi = new BufferedImage(buffImg.getWidth(),
                buffImg.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = destBi.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, buffImg.getWidth(), buffImg.getHeight());
        for (int i = 0; i < destBi.getWidth(); i++) {
            for (int j = 0; j < destBi.getHeight(); j++) {
                int nOldX = getXPosition4Twist(dPhase, dMultValue, destBi.getHeight(), i, j);
                int nOldY = j;
                if (nOldX >= 0 && nOldX < destBi.getWidth() && nOldY >= 0
                        && nOldY < destBi.getHeight()) {
                    destBi.setRGB(nOldX, nOldY, buffImg.getRGB(i, j));
                }
            }
        }
        return destBi;
    }

    /**
     * Get distorted x-axis position
     * @param dPhase
     * @param dMultValue
     * @param height
     * @param xPosition
     * @param yPosition
     * @return int
     */
    private int getXPosition4Twist(double dPhase, double dMultValue,
                                   int height, int xPosition, int yPosition) {
        double PI = 3.1415926535897932384626433832799;
        double dx = (double) (PI * yPosition) / height + dPhase;
        double dy = Math.sin(dx);
        return xPosition + (int) (dy * dMultValue);
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    char getRandomText() {
        Random random = new Random();
        return text.charAt(random.nextInt(text.length()));
    }

    /**
     * Get random color within range
     * @param fc
     * @param bc
     * @return Color
     */
    Color getRandColor(int fc, int bc) {
        Random random = new Random();
        if (fc > 255) {
            fc = 255;
        }
        if (bc > 255) {
            bc = 255;
        }
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }
}
