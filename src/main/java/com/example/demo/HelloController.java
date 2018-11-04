package com.example.demo;

import com.example.demo.generator.HtmlGenerator;
import com.example.demo.generator.SimsunFontProvider;
import com.example.demo.service.PdfService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.itextpdf.tool.xml.Pipeline;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.html.CssAppliersImpl;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.net.FileRetrieve;
import com.itextpdf.tool.xml.net.ReadingProcessor;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CSSResolver;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline;
import com.itextpdf.tool.xml.pipeline.html.*;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fop.apps.*;
import org.apache.poi.xwpf.converter.core.FileImageExtractor;
import org.apache.poi.xwpf.converter.core.FileURIResolver;
import org.apache.poi.xwpf.converter.xhtml.XHTMLConverter;
import org.apache.poi.xwpf.converter.xhtml.XHTMLOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.jsoup.Jsoup;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

@RestController
public class HelloController {
    private static final String CHARSET_NAME = "UTF-8";

    // configure fopFactory as desired
    //private final FopFactory fopFactory = FopFactory.newInstance(new File("C:\\Users\\jinchangli\\Downloads\\demo\\demo\\conf\\fop.xconf").toURI());
    //private final FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());


    /**
     * Converts an FO file to a PDF file using FOP
     * @param fo the FO file
     * @param pdf the target PDF file
     * @throws IOException In case of an I/O problem
     * @throws FOPException In case of a FOP problem
     */
    public void convertFO2PDF(String fo, OutputStream pdf) throws IOException, SAXException, ConfigurationException {

        OutputStream out = null;
        DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
        Configuration cfg = cfgBuilder.buildFromFile(new File("conf/fop.xconf"));
        FopFactoryBuilder fopFactoryBuilder = new FopFactoryBuilder(new File("conf/fop.xconf").toURI()).setConfiguration(cfg);
        FopFactory fopFactory = fopFactoryBuilder.build();

        try {
            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();

            // configure foUserAgent as desired

            // Setup output stream.  Note: Using BufferedOutputStream
            // for performance reasons (helpful with FileOutputStreams).
            //out = new FileOutputStream(pdf);
            out = new BufferedOutputStream(pdf);

            // Construct fop with desired output format
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

            // Setup JAXP using identity transformer
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(); // identity transformer

            // Setup input stream
            Source src = new StreamSource(new StringReader(fo));

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



    @RequestMapping("/hello")
    public void index(HttpServletResponse response) throws IOException {
        response.setCharacterEncoding(CHARSET_NAME);
        OutputStream out = response.getOutputStream();
        response.setContentType("application/pdf");
        response.addHeader(
                "Content-Disposition",
                "attachment; filename="
                        + String.format("%s.pdf",
                        Long.toString(System.currentTimeMillis())));
        PdfService ps = new PdfService();
        Map<String, Object> content = ps.getContent();
        try {
            String templateHtml = HtmlGenerator.generate("webTemplate.html",
                    content);
            generatePdf(templateHtml, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            out.flush();
            out.close();
        }
    }

    @RequestMapping("hello6")
    public void hello6(HttpServletResponse response) throws IOException {
        response.setCharacterEncoding(CHARSET_NAME);
        OutputStream out = response.getOutputStream();
        response.setContentType("application/pdf");
        response.addHeader(
                "Content-Disposition",
                "attachment; filename="
                        + String.format("%s.pdf",
                        Long.toString(System.currentTimeMillis())));
        PdfService ps = new PdfService();
        Map<String, Object> content = ps.getContent();
        try {
            String templateHtml = HtmlGenerator.generate("helloworld.fo",
                    content);

            convertFO2PDF(templateHtml, out);


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            out.flush();
            out.close();
        }
    }



    /**
     *
     */
    @RequestMapping("/hello5")
    public void hello5(HttpServletResponse response) throws IOException {
        Document document = new Document(PageSize.A4,70,70,36,36);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("HelloWorld.pdf"));
            BaseFont baseFont = BaseFont.createFont("simfang.ttf",BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED);
            document.open();
            Font font = new Font(baseFont,20);

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
                    Font font = new Font(bf,20);

                    //Header
                    float x = document.top(-20);

                    //中
                    /*cb.showTextAligned(PdfContentByte.ALIGN_CENTER,
                            new Phrase(),
                            (document.right() + document.left())/2,
                            x, 0);*/



                    //Footer
                    float y = document.bottom(-20);

                    //中
                    cb.showTextAligned(PdfContentByte.ALIGN_CENTER,
                            writer.getPageNumber()+"",
                            (document.right() + document.left())/2,
                            y, 0);

                    cb.endText();


                    ColumnText.showTextAligned(cb,Element.ALIGN_CENTER, new Paragraph(new Chunk(new LineSeparator(font))),
                            (document.right() + document.left())/2,
                            x,0
                    );

//code skeleton to write page header
                    PdfPTable tbl = new PdfPTable(3);
                    tbl.addCell("1st cell");
                    tbl.addCell("2nd cell");
                    tbl.addCell("3rd cell");
                    float x1 = document.leftMargin();
                    float hei =20.0F; //custom method that return header's height
                    //align bottom between page edge and page margin
                    float y1 = document.top() + hei;
                    tbl.setWidthPercentage(100F);
                    //write the table
                    tbl.writeSelectedRows(0, -1, x1, y1, writer.getDirectContent());



                    cb.restoreState();
                }
            });



            Paragraph p = new Paragraph("§1 重要提示",font);
            Paragraph p1 = new Paragraph("201*年*年度投资报告",font);
            Paragraph p3 = new Paragraph();
            Paragraph p4 = new Paragraph("金额单位：万元",font);
            LineSeparator ls = new LineSeparator(font);
            ls.setPercentage(50.0F);
            ls.setAlignment(Element.ALIGN_LEFT);
            p3.add(new Chunk(ls));

            //LineSeparator ls2 = new LineSeparator(font);
            //ls2.drawLine(writer.getDirectContent(), document.right(),document.left(),0);
            //p.add(ls2);

            //p3.setAlignment(Element.ALIGN_LEFT);
            p4.setAlignment(Element.ALIGN_RIGHT);
            //p.setAlignment(1);
            //p1.setAlignment(1);
            //Chunk c = new Chunk("基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金",font);
            document.add(p);
            //document.add(c);
            document.add(p1);
            document.newPage();

            document.add(p3);
            document.add(p4);

            document.close();
            writer.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    @RequestMapping("/hello2")
    public void hello2(HttpServletResponse response) throws IOException {
        Document document = new Document(PageSize.A4,70,70,36,36);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("HelloWorld.pdf"));
            BaseFont baseFont = BaseFont.createFont("simfang.ttf",BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED);

            Font font = new Font(baseFont,4);
            document.open();
            Paragraph p = new Paragraph("基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金",font);
            Paragraph p1 = new Paragraph("201*年*年度投资报告",font);

            //p.setAlignment(1);
            //p1.setAlignment(1);
            //Chunk c = new Chunk("基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金基金",font);
            document.add(p);
            //document.add(c);
            document.add(p1);
            document.close();
            writer.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void generatePdf(String htmlStr, OutputStream out)
            throws IOException, DocumentException {
        //final ServletContext servletContext = getServletContext();

        Document document = new Document(PageSize.A4, 30, 30, 30, 30);
        document.setMargins(30, 30, 30, 30);
        PdfWriter writer = PdfWriter.getInstance(document, out);
        document.open();

        // html内容解析
        HtmlPipelineContext htmlContext = new HtmlPipelineContext(
                new CssAppliersImpl(new XMLWorkerFontProvider() {
                    @Override
                    public Font getFont(String fontname, String encoding,
                                        float size, final int style) {
                        /*Font font = null;
                        if (fontname == null) {
                            //字体
                            String fontCn = null;

                            try {
                                fontCn = getChineseFont();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            BaseFont bf;
                            try {
                                //注意这里有一个,1
                                bf = BaseFont.createFont(fontCn+",1",
                                        BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                                font = new Font(bf, size, style);
                            } catch (DocumentException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                        return font;*/


                        if (fontname == null) {
                            // 操作系统需要有该字体, 没有则需要安装; 当然也可以将字体放到项目中， 再从项目中读取
                            fontname = "SimSun";
                        }
                        return super.getFont(fontname, encoding, size, style);
                    }
                })) {
            @Override
            public HtmlPipelineContext clone()
                    throws CloneNotSupportedException {
                HtmlPipelineContext context = super.clone();
                try {
                    ImageProvider imageProvider = this.getImageProvider();
                    context.setImageProvider(imageProvider);
                } catch (NoImageProviderException e) {
                }
                return context;
            }
        };

        // 图片解析
        /*htmlContext.setImageProvider(new AbstractImageProvider() {
            // String rootPath = servletContext.getRealPath("/");
            @Override
            public String getImageRootPath() {
                return "";
            }

            @Override
            public Image retrieve(String src) {
                if (StringUtils.isEmpty(src)) {
                    return null;
                }
                try {
                    // String imageFilePath = new File(rootPath, src).toURI().toString();
                    Image image = Image.getInstance(src);
                    image.setAbsolutePosition(400, 400);
                    if (image != null) {
                        store(src, image);
                        return image;
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return super.retrieve(src);
            }
        });*/
        htmlContext.setAcceptUnknown(true).autoBookmark(true)
                .setTagFactory(Tags.getHtmlTagProcessorFactory());

        // css解析
        CSSResolver cssResolver = XMLWorkerHelper.getInstance()
                .getDefaultCssResolver(true);
        cssResolver.setFileRetrieve(new FileRetrieve() {
            @Override
            public void processFromStream(InputStream in,
                                          ReadingProcessor processor) throws IOException {
                try (InputStreamReader reader = new InputStreamReader(in,
                        CHARSET_NAME)) {
                    int i = -1;
                    while (-1 != (i = reader.read())) {
                        processor.process(i);
                    }
                } catch (Throwable e) {
                }
            }

            // 解析href
            @Override
            public void processFromHref(String href, ReadingProcessor processor)
                    throws IOException {
                // InputStream is = servletContext.getResourceAsStream(href);
                URL url = new URL(href);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5 * 1000);
                InputStream is = conn.getInputStream();

                try (InputStreamReader reader = new InputStreamReader(is,
                        CHARSET_NAME)) {
                    int i = -1;
                    while (-1 != (i = reader.read())) {
                        processor.process(i);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });

        HtmlPipeline htmlPipeline = new HtmlPipeline(htmlContext,
                new PdfWriterPipeline(document, writer));
        Pipeline<?> pipeline = new CssResolverPipeline(cssResolver,
                htmlPipeline);
        XMLWorker worker = null;
        worker = new XMLWorker(pipeline, true);
        XMLParser parser = new XMLParser(true, worker,
                Charset.forName(CHARSET_NAME));
        try (InputStream inputStream = new ByteArrayInputStream(
                htmlStr.getBytes())) {
            parser.parse(inputStream, Charset.forName(CHARSET_NAME));
        }
        document.close();
    }

    /**
     * 获取中文字体位置
     * @return
     */
    private String getChineseFont() throws IOException {

        String chineseFont = null;
        //chineseFont = this.getClass().getResource("/").getPath() + "static/font/SIMSUN.TTC";
        //InputStream stream = getClass().getClassLoader().getResourceAsStream("static/font/SIMSUN.TTC");
        File targetFile = new File("SIMSUN.TTC");
        //FileUtils.copyInputStreamToFile(stream, targetFile);
        chineseFont = targetFile.getPath();
        if(!new File(chineseFont).exists()){
            throw new RuntimeException("字体文件不存在,影响导出pdf中文显示！"+chineseFont);
        }

        return chineseFont;
    }

    @RequestMapping("/hello4")
    public void hello4(HttpServletResponse response) throws IOException, DocumentException {
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document,new FileOutputStream("abcd.pdf"));
        document.open();
        FontProvider fp = new SimsunFontProvider();
        //XMLWorkerHelper.getInstance().parseXHtml(writer,document,new FileInputStream("abcdef.html"),getClass().getClassLoader().getResourceAsStream("static/css/pdf.css"),Charset.forName("UTF-8"),fp);
        XMLWorkerHelper.getInstance().parseXHtml(writer,document,new FileInputStream("abcdef.html"),null,Charset.forName("UTF-8"),fp);

        document.close();
    }

    /**
     * 2007版本word转换成html
     * @throws IOException
     */
    @RequestMapping("/hello3")
    public void hello3(HttpServletResponse response) throws IOException {
        String filepath = "";
        String fileName = "abcd.docx";
        String htmlName = "abcd.html";
        final String file = filepath + fileName;
        File f = new File(file);
        if (!f.exists()) {
            System.out.println("Sorry File does not Exists!");
        } else {
            if (f.getName().endsWith(".docx") || f.getName().endsWith(".DOCX")) {

                // 1) 加载word文档生成 XWPFDocument对象
                InputStream in = new FileInputStream(f);
                XWPFDocument document = new XWPFDocument(in);

                // 2) 解析 XHTML配置 (这里设置IURIResolver来设置图片存放的目录)
                File imageFolderFile = new File(filepath);
                XHTMLOptions options = XHTMLOptions.create().URIResolver(new FileURIResolver(imageFolderFile));
                options.setExtractor(new FileImageExtractor(imageFolderFile));
                options.setIgnoreStylesIfUnused(false);
                options.setFragment(true);

                // 3) 将 XWPFDocument转换成XHTML
                //OutputStream out = new FileOutputStream(new File(filepath + htmlName));
                //XHTMLConverter.getInstance().convert(document, out, options);

                //也可以使用字符数组流获取解析的内容
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                XHTMLConverter.getInstance().convert(document, baos, options);
                String content = baos.toString();
                baos.close();

                //表准化
                FileOutputStream fos = null;
                BufferedWriter bw = null;
                org.jsoup.nodes.Document doc = Jsoup.parse(content);

                //删除DIV
                /*
                String style = doc.body().attr("style");
                if(StringUtils.isNotEmpty(style) && style.indexOf("width") != -1){
                    doc.body().attr("style","");
                }
                org.jsoup.select.Elements divs = doc.select("div");
                for(int i=0; i < divs.size();i++){
                    org.jsoup.nodes.Element div = divs.get(i);
                    style = div.attr("style");
                    if(StringUtils.isNotEmpty(style) && style.indexOf("width") != -1){
                        div.attr("style","");
                    }
                }*/


                content = doc.html();

                File nfile = new File("abcdef.html");
                fos = new FileOutputStream(nfile);
                bw = new BufferedWriter(new OutputStreamWriter(fos,"UTF-8"));
                bw.write(content);
                bw.close();
                fos.close();



            } else {
                System.out.println("Enter only MS Office 2007+ files");
            }
        }
    }

}
