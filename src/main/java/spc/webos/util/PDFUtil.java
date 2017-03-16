package spc.webos.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import com.itextpdf.tool.xml.XMLWorkerHelper;

import spc.webos.config.AppConfig;
import spc.webos.constant.Common;
import spc.webos.constant.Config;

/**
 * pdf打印工具类
 * 
 */
public class PDFUtil
{
	public final static String PDF_FONT_PATH = "pdf.fontpath";
	public static final Logger log = LoggerFactory.getLogger(PDFUtil.class);

	/**
	 * html转换为pdf
	 * 
	 * @param html
	 * @param pdfPath
	 * @throws Exception
	 */
	public static void createPdf(OutputStream pdf, byte[]... htmls) throws Exception
	{
		Document document = new Document(PageSize.A4);
		PdfWriter pdfWriter = null;
		try
		{
			pdfWriter = PdfWriter.getInstance(document, pdf);
			pdfWriter.setPdfVersion(PdfWriter.PDF_VERSION_1_2);// PDF版本(默认1.4)
			pdfWriter.setMargins(0.2f, 0.2f, 0, 0);
			document.open();
			XMLWorkerHelper helper = XMLWorkerHelper.getInstance();
			for (byte[] html : htmls)
				helper.parseXHtml(pdfWriter, document, new ByteArrayInputStream(html),
						Charset.forName(Common.CHARSET_UTF8), new AsianFontProvider());
		}
		finally
		{
			try
			{
				if (document != null) document.close();
				if (pdfWriter != null) pdfWriter.close();
				if (pdf != null) pdf.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	/****
	 * 合并两个PDF
	 * 
	 * @param inPdfs
	 *            [] 要合并的PDF文件路径集合，合并顺序为数组存储顺序
	 * @param savepath
	 *            合并后的文件路径
	 * @throws Exception
	 */
	public static void mergePdf(OutputStream outPdf, InputStream... inPdfs) throws Exception
	{
		Document document = null;
		PdfCopy copy = null;
		try
		{
			document = new Document(new PdfReader(inPdfs[0]).getPageSize(1));
			copy = new PdfCopy(document, outPdf);
			document.open();
			for (InputStream pdf : inPdfs)
			{
				PdfReader reader = new PdfReader(pdf);
				int n = reader.getNumberOfPages();
				for (int j = 1; j <= n; j++)
				{
					document.newPage();
					copy.addPage(copy.getImportedPage(reader, j));
				}
			}
		}
		finally
		{
			try
			{
				for (InputStream file : inPdfs)
					file.close();
				if (outPdf != null) outPdf.close();
				if (document != null) document.close();
				if (copy != null) copy.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	/**
	 * 装饰PDF文件，添加文字
	 * 
	 * @throws Exception
	 */
	public void modifyPdfForText(OutputStream outPdf, InputStream inPdf, String text, int size,
			float x, float y) throws Exception
	{
		PdfStamper stamp = null;
		PdfReader reader = null;
		try
		{
			reader = new PdfReader(inPdf);
			int n = reader.getNumberOfPages();
			stamp = new PdfStamper(reader, outPdf);
			int i = 0;
			PdfContentByte over;
			// BaseFont bfChinese = BaseFont.createFont("STSongStd-Light",
			// "UniGB-UCS2-H",
			// BaseFont.NOT_EMBEDDED);
			BaseFont bfChinese = BaseFont.createFont(fontPath(), BaseFont.IDENTITY_H,
					BaseFont.NOT_EMBEDDED);
			while (i < n)
			{
				i++;
				over = stamp.getOverContent(i);
				over.beginText();
				over.setFontAndSize(bfChinese, size);
				over.showTextAligned(PdfContentByte.ALIGN_RIGHT, text, x, y, 0);
				over.endText();
			}
		}
		finally
		{
			try
			{
				if (outPdf != null) outPdf.close();
				if (inPdf != null) inPdf.close();
				if (stamp != null) stamp.close();
				if (reader != null) reader.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	/**
	 * 装饰PDF文件，添加图片
	 *
	 * @throws Exception
	 */
	public void modifyPdfForImage(OutputStream outPdf, InputStream inPdf, byte[] image, int size,
			float x, float y) throws Exception
	{
		PdfStamper stamp = null;
		PdfReader reader = null;
		try
		{
			reader = new PdfReader(inPdf);
			int n = reader.getNumberOfPages();
			stamp = new PdfStamper(reader, outPdf);
			int i = 0;
			PdfContentByte under;
			Image img = Image.getInstance(image);
			img.scalePercent(size);
			img.setAbsolutePosition(x, y);
			while (i < n)
			{
				i++;
				under = stamp.getUnderContent(i);
				under.addImage(img);
			}
		}
		finally
		{
			try
			{
				if (outPdf != null) outPdf.close();
				if (inPdf != null) inPdf.close();
				if (stamp != null) stamp.close();
				if (reader != null) reader.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	public static void main(String[] args) throws Exception
	{
		// System.setProperty(PDFUtil.PDF_FONT_PATH,
		// "/Users/chenjs/software/tomcat9/conf/simsun.ttf");
		System.setProperty(PDFUtil.PDF_FONT_PATH, "/Users/chenjs/Downloads/simsun.ttf");
		createPdf(new FileOutputStream("/Users/chenjs/Downloads/test.pdf"),
				FileUtil.file2bytes(new File("/Users/chenjs/Downloads/pdf.htm")),
				FileUtil.file2bytes(new File("/Users/chenjs/Downloads/pdf.htm")));
		// mergePdf(new FileOutputStream("/Users/chenjs/Downloads/test-m.pdf"),
		// "/Users/chenjs/Downloads/test-1.pdf",
		// "/Users/chenjs/Downloads/test-2.pdf");
		// // 创建基础字体
		// BaseFont bf = BaseFont.createFont(PDFUtil.FONT_PATH,
		// BaseFont.IDENTITY_H,
		// BaseFont.EMBEDDED);
		// // 自定义字体属性
		// Font font = new Font(bf, 30);
		//
		// Document document = new Document(PageSize.A4.rotate());
		// PdfWriter writer = PdfWriter.getInstance(document,
		// new FileOutputStream("/Users/chenjs/Downloads/test_cn.pdf"));
		// writer.setPdfVersion(PdfWriter.PDF_VERSION_1_7);
		// // Make document tagged
		// writer.setTagged();
		// // ===============
		// writer.setViewerPreferences(PdfWriter.DisplayDocTitle);
		// document.addTitle("中文测试");
		// writer.createXmpMetadata();
		// // =====================
		// document.open();
		// Paragraph p = new Paragraph();
		// // Embed font
		// p.setFont(font);
		// // ==================
		// Chunk c = new Chunk("中文测试");
		// p.add(c);
		// document.add(p);
		// Font cfont = new Font(bf, 64);
		// Phrase ph = new Phrase("中文测试", cfont);
		// document.add(ph);
		// document.close();
	}

	static String fontPath()
	{
		String fontPath = AppConfig.getInstance().getProperty(Config.app_pdf_fontpath, false,
				System.getProperty(PDFUtil.PDF_FONT_PATH));
		// if (fontPath == null || fontPath.length() == 0)
		// {
		// fontPath =
		// Thread.currentThread().getContextClassLoader().getResource("simsun.ttf")
		// .getFile();
		// }
		return fontPath;
	}

	public static class AsianFontProvider extends XMLWorkerFontProvider
	{
		public Font getFont(final String fontname, final String encoding, final boolean embedded,
				final float size, final int style, final BaseColor color)
		{
			String fontPath = "";
			try
			{
				// BaseFont f = BaseFont.createFont("STSong-Light",
				// "UniGB-UCS2-H",
				// BaseFont.NOT_EMBEDDED);
				BaseFont f = BaseFont.createFont(fontPath = fontPath(), BaseFont.IDENTITY_H,
						BaseFont.NOT_EMBEDDED);
				return new Font(f, size, style, color);
			}
			catch (DocumentException | IOException e)
			{
				PDFUtil.log.error("check font path:{}, check system property: -Dpdf.fontpath={}",
						fontPath, System.getProperty(PDFUtil.PDF_FONT_PATH), e);
				throw new RuntimeException(e);
			}
		}
	}
}
