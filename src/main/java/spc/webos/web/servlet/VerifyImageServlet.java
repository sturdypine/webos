package spc.webos.web.servlet;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import spc.webos.config.AppConfig;
import spc.webos.constant.Config;
import spc.webos.web.common.SUI;

public class VerifyImageServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		doRequest(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		doRequest(request, response);
	}

	protected void doRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{

		response.setContentType("image/gif");
		response.setHeader("Cache-Control", "no-cache");
		int width = 60, height = 20,
				num = AppConfig.getInstance().getProperty(Config.app_login_verify_len, false, 4);
		String strWidth = (String) request.getParameter("w");
		String strHeight = (String) request.getParameter("h");
		if (strWidth != null && strWidth.length() > 0) width = Integer.parseInt(strWidth);
		if (strHeight != null && strHeight.length() > 0) height = Integer.parseInt(strHeight);

		ServletOutputStream out = response.getOutputStream();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); // 设置图片大小的
		Graphics gra = image.getGraphics();
		Random random = new Random();

		gra.setColor(getRandColor(200, 250)); // 设置背景色
		gra.fillRect(0, 0, width, height);

		gra.setColor(Color.black); // 设置字体色
		gra.setFont(mFont);

		/*
		 * gra.setColor(new Color(0)); gra.drawRect(0,0,width-1,height-1);
		 */

		// 随机产生155条干扰线，使图象中的认证码不易被其它程序探测到
		gra.setColor(getRandColor(160, 200));
		for (int i = 0; i < 155; i++)
		{
			int x = random.nextInt(width);
			int y = random.nextInt(height);
			int xl = random.nextInt(12);
			int yl = random.nextInt(12);
			gra.drawLine(x, y, x + xl, y + yl);
		}

		// 取随机产生的认证码(4位数字)
		String sRand = "";
		for (int i = 0; i < num; i++)
		{
			String rand = String.valueOf(random.nextInt(10));
			sRand += rand;
			// 将认证码显示到图象中
			gra.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110),
					20 + random.nextInt(110)));// 调用函数出来的颜色相同，可能是因为种子太接近，所以只能直接生成
			gra.drawString(rand, 13 * i + 6, 16);
		}
		SUI sui = SUI.SUI.get();
		if (sui != null)
		{
			sui.setVerifyCode(sRand);
			log.info("verify image:{}", sRand);
		}
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
		encoder.encode(image);
		out.close();
	}

	static Color getRandColor(int fc, int bc)
	{// 给定范围获得随机颜色
		Random random = new Random();
		if (fc > 255) fc = 255;
		if (bc > 255) bc = 255;
		int r = fc + random.nextInt(bc - fc);
		int g = fc + random.nextInt(bc - fc);
		int b = fc + random.nextInt(bc - fc);
		return new Color(r, g, b);
	}

	Font mFont = new Font("Times New Roman", Font.PLAIN, 20);// 设置字体
	protected Logger log = LoggerFactory.getLogger(getClass());
}
