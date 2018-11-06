package com.example.demo;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.*;
import org.apache.fop.fonts.apps.TTFReader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

@SpringBootApplication
public class DemoApplication {

	// configure fopFactory as desired
	private final FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());

	static {
		System.setProperty("org.apache.commons.logging.Log",
				"org.apache.commons.logging.impl.NoOpLog");
	}


	/**
	 * Converts an FO file to a PDF file using FOP
	 * @param fo the FO file
	 * @param pdf the target PDF file
	 * @throws IOException In case of an I/O problem
	 * @throws FOPException In case of a FOP problem
	 */
	public void convertFO2PDF(File fo, File pdf) throws IOException, FOPException {

		OutputStream out = null;

		try {
			FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
			// configure foUserAgent as desired

			// Setup output stream.  Note: Using BufferedOutputStream
			// for performance reasons (helpful with FileOutputStreams).
			out = new FileOutputStream(pdf);
			out = new BufferedOutputStream(out);

			// Construct fop with desired output format
			Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

			// Setup JAXP using identity transformer
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer(); // identity transformer

			// Setup input stream
			Source src = new StreamSource(fo);

			// Resulting SAX events (the generated FO) must be piped through to FOP
			Result res = new SAXResult(fop.getDefaultHandler());

			// Start XSLT transformation and FOP processing
			transformer.transform(src, res);

			// Result processing
			FormattingResults foResults = fop.getResults();
			java.util.List pageSequences = foResults.getPageSequences();
			for (Object pageSequence : pageSequences) {
				PageSequenceResults pageSequenceResults = (PageSequenceResults) pageSequence;
				System.out.println("PageSequence "
						+ (String.valueOf(pageSequenceResults.getID()).length() > 0
						? pageSequenceResults.getID() : "<no id>")
						+ " generated " + pageSequenceResults.getPageCount() + " pages.");
			}
			System.out.println("Generated " + foResults.getPageCount() + " pages in total.");

		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(-1);
		} finally {
			out.close();
		}
	}



	public static void main(String[] args) throws DocumentException, FileNotFoundException {

		/*String[] parameters = {
				"-d",
				"-ttcname",
				"calibri",
				"calibri.ttf", "calibri.xml", };


		TTFReader.main(parameters);*/

		SpringApplication.run(DemoApplication.class, args);

		/*
		try {
			System.out.println("FOP ExampleFO2PDF\n");
			System.out.println("Preparing...");

			//Setup directories
			File baseDir = new File(".");
			File outDir = new File(baseDir, "out");
			outDir.mkdirs();

			//Setup input and output files
			File fofile = new File(baseDir, "helloworld.fo");
			//File fofile = new File(baseDir, "../fo/pagination/franklin_2pageseqs.fo");
			File pdffile = new File(outDir, "ResultFO2PDF.pdf");

			System.out.println("Input: XSL-FO (" + fofile + ")");
			System.out.println("Output: PDF (" + pdffile + ")");
			System.out.println();
			System.out.println("Transforming...");

			DemoApplication app = new DemoApplication();
			app.convertFO2PDF(fofile, pdffile);

			System.out.println("Success!");
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(-1);
		}
*/



















/*
		Document doc = new Document();
		PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream( "setHeaderFooter.pdf"));

		writer.setPageEvent(new PdfPageEventHelper() {

			public void onEndPage(PdfWriter writer, Document document) {

				PdfContentByte cb = writer.getDirectContent();
				cb.saveState();

				cb.beginText();
				BaseFont bf = null;
				try {
					bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
				} catch (Exception e) {
					e.printStackTrace();
				}
				cb.setFontAndSize(bf, 10);

				//Header
				float x = document.top(-20);

				//左
				cb.showTextAligned(PdfContentByte.ALIGN_LEFT,
						"H-Left",
						document.left(), x, 0);
				//中
				cb.showTextAligned(PdfContentByte.ALIGN_CENTER,
						writer.getPageNumber()+ " page",
						(document.right() + document.left())/2,
						x, 0);
				//右
				cb.showTextAligned(PdfContentByte.ALIGN_RIGHT,
						"H-Right",
						document.right(), x, 0);

				//Footer
				float y = document.bottom(-20);

				//左
				cb.showTextAligned(PdfContentByte.ALIGN_LEFT,
						"F-Left",
						document.left(), y, 0);
				//中
				cb.showTextAligned(PdfContentByte.ALIGN_CENTER,
						writer.getPageNumber()+" page",
						(document.right() + document.left())/2,
						y, 0);
				//右
				cb.showTextAligned(PdfContentByte.ALIGN_RIGHT,
						"F-Right",
						document.right(), y, 0);

				cb.endText();

				cb.restoreState();
			}
		});

		doc.open();
		doc.add(new Paragraph("1 page"));
		doc.newPage();
		doc.add(new Paragraph("2 page"));
		doc.newPage();
		doc.add(new Paragraph("3 page"));
		doc.newPage();
		doc.add(new Paragraph("4 page"));
		doc.close();
		writer.close();*/
	}
}
