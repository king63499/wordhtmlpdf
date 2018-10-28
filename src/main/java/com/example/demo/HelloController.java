package com.example.demo;

import com.example.demo.generator.HtmlGenerator;
import com.example.demo.generator.SimsunFontProvider;
import com.example.demo.service.PdfService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.converter.core.FileImageExtractor;
import org.apache.poi.xwpf.converter.core.FileURIResolver;
import org.apache.poi.xwpf.converter.xhtml.XHTMLConverter;
import org.apache.poi.xwpf.converter.xhtml.XHTMLOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.jsoup.Jsoup;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

@RestController
public class HelloController {
    private static final String CHARSET_NAME = "UTF-8";

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
            String templateHtml = HtmlGenerator.generate("mytemplate.html",
                    content);
            generatePdf(templateHtml, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            out.flush();
            out.close();
        }
    }

    @RequestMapping("/hello2")
    public void hello2(HttpServletResponse response) throws IOException {
        Document document = new Document();
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("HelloWorld.pdf"));
            BaseFont baseFont = BaseFont.createFont("SIMSUN.TTC"+",1",BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED);
//this.getClass().getResource("/").getPath() + "font/SIMSUN.TTC";
            Font font = new Font(baseFont);
            document.open();
            document.add(new Paragraph("这是一个中文oooo",font));
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
                        Font font = null;
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
                        return font;
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
                }


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
