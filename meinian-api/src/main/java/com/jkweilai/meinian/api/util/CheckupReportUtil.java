package com.jkweilai.meinian.api.util;

import cn.hutool.core.map.MapUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import lombok.SneakyThrows;
import org.apache.poi.util.Units;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CheckupReportUtil {

    /**
     * 创建报告封面页
     */
    private void createCover(XWPFDocument doc, HashMap param) throws Exception {
        // 创建标题段落 - 机构名称
        XWPFParagraph paragraph = doc.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setBold(true);
        run.setFontSize(20);
        run.setFontFamily("Microsoft YaHei");
        run.setUnderline(UnderlinePatterns.THICK);
        run.setText("天津美年大健康体检中心");

        // 设置段落行间距
        CTP ctp = paragraph.getCTP();
        CTPPr ctpPr = ctp.addNewPPr();
        CTSpacing ctSpacing = ctpPr.addNewSpacing();
        ctSpacing.setLineRule(STLineSpacingRule.EXACT);
        ctSpacing.setLine(BigInteger.valueOf(480));

        // 创建英文机构名称段落
        paragraph = doc.createParagraph();
        run = paragraph.createRun();
        run.setFontSize(10);
        run.setFontFamily("Microsoft YaHei");
        run.setText("Tianjin Meinian Onehealth Healthcare Center");

        // 设置英文名称段落行间距
        ctp = paragraph.getCTP();
        ctpPr = ctp.addNewPPr();
        ctSpacing = ctpPr.addNewSpacing();
        ctSpacing.setLineRule(STLineSpacingRule.EXACT);
        ctSpacing.setLine(BigInteger.valueOf(280));

        // 创建主标题段落 - 体检报告书
        paragraph = doc.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        run = paragraph.createRun();
        run.setBold(true);
        run.setFontSize(26);
        run.setFontFamily("Microsoft YaHei");
        run.setText("体检报告书");

        // 设置主标题段落间距
        ctp = paragraph.getCTP();
        ctpPr = ctp.addNewPPr();
        ctSpacing = ctpPr.addNewSpacing();
        ctSpacing.setBeforeLines(BigInteger.valueOf(800));
        ctSpacing.setAfterLines(BigInteger.valueOf(800));

        // 生成二维码
        QrConfig qrConfig = new QrConfig();
        qrConfig.setWidth(150);
        qrConfig.setHeight(150);
        qrConfig.setMargin(2);
        String uuid = MapUtil.getStr(param, "uuid");
        BufferedImage bufferedImage = QrCodeUtil.generate(uuid, qrConfig);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpg", bout);
        InputStream input = new ByteArrayInputStream(bout.toByteArray());

        // 创建二维码段落
        paragraph = doc.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        ctp = paragraph.getCTP();
        ctpPr = ctp.addNewPPr();
        ctSpacing = ctpPr.addNewSpacing();
        ctSpacing.setAfterLines(BigInteger.valueOf(400));
        run = paragraph.createRun();
        run.addPicture(input, Document.PICTURE_TYPE_JPEG, "", Units.pixelToEMU(bufferedImage.getWidth()), Units.pixelToEMU(bufferedImage.getHeight()));

        // 创建个人信息表格
        XWPFTable table = doc.createTable(4, 2);
        table.setTableAlignment(TableRowAlign.CENTER);
        table.getCTTbl().getTblPr().unsetTblBorders(); // 隐藏表格边框

        // 填充个人信息
        ArrayList<HashMap> list = (ArrayList) param.get("item");
        for (int i = 0; i < list.size(); i++) {
            HashMap map = list.get(i);
            String label = MapUtil.getStr(map, "label");
            String value = MapUtil.getStr(map, "value");
            XWPFTableRow row = table.getRow(i);
            row.setHeight(600);
            List<XWPFTableCell> tableCells = row.getTableCells();

            // 设置第二列单元格宽度
            CTTcPr ctTcPr = tableCells.get(1).getCTTc().addNewTcPr();
            CTTblWidth ctTblWidth = ctTcPr.addNewTcW();
            ctTblWidth.setW(BigInteger.valueOf(2800));

            // 标签单元格
            paragraph = tableCells.get(0).getParagraphArray(0);
            run = paragraph.createRun();
            run.setFontFamily("Microsoft YaHei");
            run.setText(label + "    ");

            // 值单元格 - 带下划线样式
            paragraph = tableCells.get(1).getParagraphArray(0);
            paragraph.setBorderBottom(Borders.BABY_RATTLE);
            run = paragraph.createRun();
            run.setFontFamily("Microsoft YaHei");
            run.setText("    " + value);
        }
    }

    /**
     * 创建欢迎致辞页面
     */
    private void createWelcome(XWPFDocument doc, HashMap param) {
        // 创建标题段落
        XWPFParagraph paragraph = doc.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = paragraph.createRun();
        run.setFontSize(18);
        run.setFontFamily("Microsoft YaHei");
        run.setText("健康体检报告");

        // 设置标题段落间距
        CTP ctp = paragraph.getCTP();
        CTPPr ctpPr = ctp.addNewPPr();
        CTSpacing ctSpacing = ctpPr.addNewSpacing();
        ctSpacing.setLineRule(STLineSpacingRule.EXACT);
        ctSpacing.setLine(BigInteger.valueOf(420));
        ctSpacing.setBeforeLines(BigInteger.valueOf(2000));
        ctSpacing.setAfterLines(BigInteger.valueOf(200));

        // 创建称呼段落
        paragraph = doc.createParagraph();
        run = paragraph.createRun();
        run.setFontSize(10);
        run.setFontFamily("Microsoft YaHei");
        String name = MapUtil.getStr(param, "name");
        String sex = MapUtil.getStr(param, "sex");
        String temp = name + (sex.equals("男") ? "先生" : "女士");
        run.setText("尊敬的" + temp + "，您好！");

        // 设置称呼段落间距
        ctp = paragraph.getCTP();
        ctpPr = ctp.addNewPPr();
        ctSpacing = ctpPr.addNewSpacing();
        ctSpacing.setLineRule(STLineSpacingRule.EXACT);
        ctSpacing.setLine(BigInteger.valueOf(380));
        ctSpacing.setAfterLines(BigInteger.valueOf(50));

        // 创建正文段落
        paragraph = doc.createParagraph();
        paragraph.setIndentationFirstLine(600); // 首行缩进
        run = paragraph.createRun();
        run.setFontSize(10);
        run.setFontFamily("Microsoft YaHei");
        run.setText("感谢您到天津美年大健康中心体检。现将您的体检结果汇总如下，请您认真阅读体检结果和建议。如有疑问，请您来院或者致电本中心服务电话022-88888888，我们将安排专业人员为您答疑解惑。欢迎对我们的工作提出批评和建议。祝您健康！");

        // 设置正文段落间距
        ctp = paragraph.getCTP();
        ctpPr = ctp.addNewPPr();
        ctSpacing = ctpPr.addNewSpacing();
        ctSpacing.setLineRule(STLineSpacingRule.EXACT);
        ctSpacing.setLine(BigInteger.valueOf(380));
        ctSpacing.setAfterLines(BigInteger.valueOf(100));
    }

    /**
     * 创建体检人信息表格
     */
    private void createCustomerInfo(XWPFDocument doc, HashMap param) {
        // 创建标题段落
        XWPFParagraph paragraph = doc.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setFontSize(14);
        run.setFontFamily("Microsoft YaHei");
        run.setText("体检人信息");

        // 设置标题段落间距
        CTP ctp = paragraph.getCTP();
        CTPPr ctpPr = ctp.addNewPPr();
        CTSpacing ctSpacing = ctpPr.addNewSpacing();
        ctSpacing.setBeforeLines(BigInteger.valueOf(200));
        ctSpacing.setAfterLines(BigInteger.valueOf(100));

        // 创建4行6列的信息表格
        XWPFTable table = doc.createTable(4, 6);
        CTTblPr tblPr = table.getCTTbl().getTblPr();
        tblPr.getTblW().setType(STTblWidth.DXA);
        tblPr.getTblW().setW(BigInteger.valueOf(9850)); // 设置表格总宽度

        // 第一行：姓名、性别、出生日期
        XWPFTableRow row = table.getRow(0);
        row.setHeight(550);
        List<XWPFTableCell> tableCells = row.getTableCells();

        // 姓名标签单元格
        XWPFTableCell cell = tableCells.get(0);
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        cell.setColor("f0f0f0"); // 设置背景色
        paragraph = cell.getParagraphArray(0);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        run = paragraph.createRun();
        run.setFontSize(9);
        run.setFontFamily("Microsoft YaHei");
        run.setText("姓名");
        CTTcPr ctTcPr = cell.getCTTc().addNewTcPr();
        CTTblWidth ctTblWidth = ctTcPr.addNewTcW();
        ctTblWidth.setW(BigInteger.valueOf(1500)); // 设置列宽

        // 姓名值单元格
        cell = tableCells.get(1);
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        paragraph = cell.getParagraphArray(0);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        run = paragraph.createRun();
        run.setFontSize(9);
        run.setFontFamily("Microsoft YaHei");
        run.setText(MapUtil.getStr(param, "name"));

        // 性别标签单元格
        cell = tableCells.get(2);
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        cell.setColor("f0f0f0");
        paragraph = cell.getParagraphArray(0);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        run = paragraph.createRun();
        run.setFontSize(9);
        run.setFontFamily("Microsoft YaHei");
        run.setText("性别");
        ctTcPr = cell.getCTTc().addNewTcPr();
        ctTblWidth = ctTcPr.addNewTcW();
        ctTblWidth.setW(BigInteger.valueOf(1500));

        // 性别值单元格
        cell = tableCells.get(3);
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        paragraph = cell.getParagraphArray(0);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        run = paragraph.createRun();
        run.setFontSize(9);
        run.setFontFamily("Microsoft YaHei");
        run.setText(MapUtil.getStr(param, "sex"));
        ctTcPr = cell.getCTTc().addNewTcPr();
        ctTblWidth = ctTcPr.addNewTcW();
        ctTblWidth.setW(BigInteger.valueOf(1200));

        // 出生日期标签单元格
        cell = tableCells.get(4);
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        cell.setColor("f0f0f0");
        paragraph = cell.getParagraphArray(0);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        run = paragraph.createRun();
        run.setFontSize(9);
        run.setFontFamily("Microsoft YaHei");
        run.setText("出生日期");
        ctTcPr = cell.getCTTc().addNewTcPr();
        ctTblWidth = ctTcPr.addNewTcW();
        ctTblWidth.setW(BigInteger.valueOf(1500));

        // 出生日期值单元格
        cell = tableCells.get(5);
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        paragraph = cell.getParagraphArray(0);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        run = paragraph.createRun();
        run.setFontSize(9);
        run.setFontFamily("Microsoft YaHei");
        run.setText(MapUtil.getStr(param, "birthday"));

        // 第二行：电话、年龄、体检日期
        row = table.getRow(1);
        row.setHeight(550);
        tableCells = row.getTableCells();

        // 电话标签单元格
        cell = tableCells.get(0);
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        cell.setColor("f0f0f0");
        paragraph = cell.getParagraphArray(0);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        run = paragraph.createRun();
        run.setFontSize(9);
        run.setFontFamily("Microsoft YaHei");
        run.setText("电话");

        // 电话值单元格
        cell = tableCells.get(1);
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        paragraph = cell.getParagraphArray(0);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        run = paragraph.createRun();
        run.setFontSize(9);
        run.setFontFamily("Microsoft YaHei");
        run.setText(MapUtil.getStr(param, "tel"));

        // 年龄标签单元格
        cell = tableCells.get(2);
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        cell.setColor("f0f0f0");
        paragraph = cell.getParagraphArray(0);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        run = paragraph.createRun();
        run.setFontSize(9);
        run.setFontFamily("Microsoft YaHei");
        run.setText("年龄");

        // 年龄值单元格
        cell = tableCells.get(3);
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        paragraph = cell.getParagraphArray(0);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        run = paragraph.createRun();
        run.setFontSize(9);
        run.setFontFamily("Microsoft YaHei");
        run.setText(MapUtil.getStr(param, "age"));

        // 体检日期标签单元格
        cell = tableCells.get(4);
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        cell.setColor("f0f0f0");
        paragraph = cell.getParagraphArray(0);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        run = paragraph.createRun();
        run.setFontSize(9);
        run.setFontFamily("Microsoft YaHei");
        run.setText("体检日期");

        // 体检日期值单元格
        cell = tableCells.get(5);
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        paragraph = cell.getParagraphArray(0);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        run = paragraph.createRun();
        run.setFontSize(9);
        run.setFontFamily("Microsoft YaHei");
        run.setText(MapUtil.getStr(param, "date"));

        // 第三行：单位名称（跨列合并）
        row = table.getRow(2);
        row.setHeight(550);
        tableCells = row.getTableCells();

        // 单位名称标签单元格
        cell = tableCells.get(0);
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        cell.setColor("f0f0f0");
        paragraph = cell.getParagraphArray(0);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        run = paragraph.createRun();
        run.setFontSize(9);
        run.setFontFamily("Microsoft YaHei");
        run.setText("单位名称");

        // 单位名称值单元格 - 横向合并
        cell = tableCells.get(1);
        cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.RESTART);
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        paragraph = cell.getParagraphArray(0);
        paragraph.setAlignment(ParagraphAlignment.LEFT);
        run = paragraph.createRun();
        run.setFontSize(9);
        run.setFontFamily("Microsoft YaHei");
        run.setText("    " + param.get("company"));

        // 合并后续单元格
        for (int i = 2; i <= 5; i++) {
            cell = tableCells.get(i);
            cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
        }

        // 第四行：体检套餐（跨列合并）
        row = table.getRow(3);
        row.setHeight(600);
        tableCells = row.getTableCells();

        // 体检套餐标签单元格
        cell = tableCells.get(0);
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        cell.setColor("f0f0f0");
        paragraph = cell.getParagraphArray(0);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        run = paragraph.createRun();
        run.setFontSize(9);
        run.setFontFamily("Microsoft YaHei");
        run.setText("体检套餐");

        // 体检套餐值单元格 - 横向合并
        cell = tableCells.get(1);
        cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.RESTART);
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        paragraph = cell.getParagraphArray(0);
        paragraph.setAlignment(ParagraphAlignment.LEFT);
        run = paragraph.createRun();
        run.setFontSize(9);
        run.setFontFamily("Microsoft YaHei");
        run.setText("    " + param.get("goods"));

        // 合并后续单元格
        for (int i = 2; i <= 5; i++) {
            cell = tableCells.get(i);
            cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
        }
    }

    /**
     * 创建体检内容表格
     */
    private void createCheckup(XWPFDocument doc, List<Map> list) {
        // 创建标题段落
        XWPFParagraph paragraph = doc.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setFontSize(14);
        run.setFontFamily("Microsoft YaHei");
        run.setText("体检内容");

        // 设置标题段落间距
        CTP ctp = paragraph.getCTP();
        CTPPr ctpPr = ctp.addNewPPr();
        CTSpacing ctSpacing = ctpPr.addNewSpacing();
        ctSpacing.setBeforeLines(BigInteger.valueOf(200));
        ctSpacing.setAfterLines(BigInteger.valueOf(100));

        // 创建体检内容表格
        XWPFTable table = doc.createTable(list.size() + 1, 3);
        CTTblPr tblPr = table.getCTTbl().getTblPr();
        tblPr.getTblW().setType(STTblWidth.DXA);
        tblPr.getTblW().setW(BigInteger.valueOf(9850));

        // 表头行
        String[] array = {"序号", "体检科室", "体检内容"};
        XWPFTableRow row = table.getRow(0);
        row.setHeight(550);
        List<XWPFTableCell> tableCells = row.getTableCells();
        for (int i = 0; i < array.length; i++) {
            String text = array[i];
            XWPFTableCell cell = tableCells.get(i);
            cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
            cell.setColor("f0f0f0"); // 表头背景色
            paragraph = cell.getParagraphArray(0);
            paragraph.setAlignment(ParagraphAlignment.CENTER);
            run = paragraph.createRun();
            run.setFontSize(9);
            run.setFontFamily("Microsoft YaHei");
            run.setText(text);
        }

        // 填充体检内容数据，支持科室合并
        String temp = null;
        int index = 0;
        for (int i = 0; i < list.size(); i++) {
            Map one = list.get(i);
            String place = MapUtil.getStr(one, "place");
            String item = MapUtil.getStr(one, "item");
            row = table.getRow(i + 1);
            row.setHeight(550);
            tableCells = row.getTableCells();

            // 处理科室列的纵向合并
            XWPFTableCell cell = tableCells.get(1);
            STMerge.Enum val = null;
            if (!place.equals(temp)) {
                index++;
                temp = place;
                val = STMerge.RESTART; // 开始合并
                cell.getCTTc().addNewTcPr().addNewVMerge().setVal(val);
            } else {
                val = STMerge.CONTINUE; // 继续合并
                cell.getCTTc().addNewTcPr().addNewVMerge().setVal(val);
            }
            cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
            paragraph = cell.getParagraphArray(0);
            paragraph.setAlignment(ParagraphAlignment.CENTER);
            run = paragraph.createRun();
            run.setFontSize(9);
            run.setFontFamily("Microsoft YaHei");
            run.setText(place);

            // 序号列同样需要纵向合并
            cell = tableCells.get(0);
            cell.getCTTc().addNewTcPr().addNewVMerge().setVal(val);
            cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
            paragraph = cell.getParagraphArray(0);
            paragraph.setAlignment(ParagraphAlignment.CENTER);
            run = paragraph.createRun();
            run.setFontSize(9);
            run.setFontFamily("Microsoft YaHei");
            run.setText(index + "");

            // 体检内容列
            cell = tableCells.get(2);
            cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
            paragraph = cell.getParagraphArray(0);
            paragraph.setAlignment(ParagraphAlignment.CENTER);
            run = paragraph.createRun();
            run.setFontSize(9);
            run.setFontFamily("Microsoft YaHei");
            run.setText(item);
        }
    }

    /**
     * 模板1：创建体检结果表格（基础表格样式）
     */
    private void createCheckupResultByTemplate_1(XWPFDocument doc, Map param) {
        String place = MapUtil.getStr(param, "place");
        List<Map> item = MapUtil.get(param, "item", List.class);
        String doctor = MapUtil.getStr(param, "doctorName");
        String date = MapUtil.getStr(param, "date");

        // 创建科室标题段落
        XWPFParagraph paragraph = doc.createParagraph();
        paragraph.setBorderBottom(Borders.BABY_RATTLE); // 底部边框
        XWPFRun run = paragraph.createRun();
        run.setFontSize(14);
        run.setFontFamily("Microsoft YaHei");
        run.setTextHighlightColor("lightGray"); // 文字高亮
        run.setText("【" + place + "体检结果】");

        // 设置标题段落间距
        CTP ctp = paragraph.getCTP();
        CTPPr ctpPr = ctp.addNewPPr();
        CTSpacing ctSpacing = ctpPr.addNewSpacing();
        ctSpacing.setBeforeLines(BigInteger.valueOf(250));
        ctSpacing.setAfterLines(BigInteger.valueOf(150));

        // 创建体检结果表格
        XWPFTable table = doc.createTable(item.size() + 1, 5);
        CTTblPr tblPr = table.getCTTbl().getTblPr();
        tblPr.getTblW().setType(STTblWidth.DXA);
        tblPr.getTblW().setW(BigInteger.valueOf(9850));

        // 表头行
        XWPFTableRow row = table.getRow(0);
        row.setHeight(500);
        List<XWPFTableCell> tableCells = row.getTableCells();
        String[] array = {"序号#800", "检查项目#2200", "检查结果#3000", "单位#1200", "参考值#1300",};
        for (int i = 0; i <= 4; i++) {
            XWPFTableCell cell = tableCells.get(i);
            cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
            cell.setColor("f0f0f0"); // 表头背景色
            paragraph = cell.getParagraphArray(0);
            if (i != 2) {
                paragraph.setAlignment(ParagraphAlignment.CENTER);
            } else {
                paragraph.setIndentationLeft(200); // 检查结果列左缩进
            }
            run = paragraph.createRun();
            run.setFontSize(9);
            run.setFontFamily("Microsoft YaHei");
            String label = array[i].split("#")[0];
            int width = Integer.parseInt(array[i].split("#")[1]);
            run.setText(label);
            // 设置列宽
            CTTcPr ctTcPr = cell.getCTTc().addNewTcPr();
            CTTblWidth ctTblWidth = ctTcPr.addNewTcW();
            ctTblWidth.setW(BigInteger.valueOf(width));
        }

        // 填充体检结果数据
        for (int i = 0; i < item.size(); i++) {
            Map map = item.get(i);
            String name = MapUtil.getStr(map, "name");
            String value = MapUtil.getStr(map, "value");
            String unit = MapUtil.getStr(map, "unit");
            unit = (unit == null ? "" : unit);
            String standard = MapUtil.getStr(map, "standard");
            standard = (standard == null ? "" : standard);
            String[] temp = {i + 1 + "", name, value, unit, standard};
            row = table.getRow(i + 1);
            row.setHeight(500);
            tableCells = row.getTableCells();
            for (int j = 0; j < temp.length; j++) {
                XWPFTableCell cell = tableCells.get(j);
                cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
                paragraph = cell.getParagraphArray(0);
                if (j != 2) {
                    paragraph.setAlignment(ParagraphAlignment.CENTER);
                } else {
                    paragraph.setIndentationLeft(200); // 检查结果列左缩进
                }
                run = paragraph.createRun();
                run.setFontSize(9);
                run.setFontFamily("Microsoft YaHei");
                run.setText(temp[j]);
            }
        }

        // 创建医生签名和日期段落
        paragraph = doc.createParagraph();
        run = paragraph.createRun();
        run.setFontSize(9);
        run.setFontFamily("Microsoft YaHei");
        run.setText("体检医生：" + doctor + "\t\t\t\t\t\t\t\t\t日期：" + date);
        ctp = paragraph.getCTP();
        ctpPr = ctp.addNewPPr();
        ctSpacing = ctpPr.addNewSpacing();
        ctSpacing.setBeforeLines(BigInteger.valueOf(100));
        ctSpacing.setAfterLines(BigInteger.valueOf(150));
    }

    /**
     * 模板2：创建带图片的体检结果表格
     */
    @SneakyThrows
    private void createCheckupResultByTemplate_2(XWPFDocument doc, Map param) {
        String place = MapUtil.getStr(param, "place");
        List<Map> item = (List<Map>) param.get("item");
        String doctor = MapUtil.getStr(param, "doctorName");
        String date = MapUtil.getStr(param, "date");
        String image = MapUtil.getStr(param, "image");

        // 创建科室标题段落
        XWPFParagraph paragraph = doc.createParagraph();
        paragraph.setBorderBottom(Borders.BABY_RATTLE);
        XWPFRun run = paragraph.createRun();
        run.setFontSize(14);
        run.setFontFamily("Microsoft YaHei");
        run.setTextHighlightColor("lightGray");
        run.setText("【" + place + "体检结果】");

        // 设置标题段落间距
        CTP ctp = paragraph.getCTP();
        CTPPr ctpPr = ctp.addNewPPr();
        CTSpacing ctSpacing = ctpPr.addNewSpacing();
        ctSpacing.setBeforeLines(BigInteger.valueOf(250));
        ctSpacing.setAfterLines(BigInteger.valueOf(150));

        // 插入图片（如医学影像）
        if (image != null) {
            URL url = new URL(image);
            InputStream in = url.openStream();
            BufferedImage bufferedImage = ImageIO.read(url);
            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();
            double scaling = 1.0;
            // 图片宽度自适应
            if (width > 72 * 9.13) {
                scaling = (72.0 * 9.13) / width;
            }
            paragraph = doc.createParagraph();
            paragraph.setAlignment(ParagraphAlignment.LEFT);
            ctp = paragraph.getCTP();
            ctpPr = ctp.addNewPPr();
            ctSpacing = ctpPr.addNewSpacing();
            ctSpacing.setAfterLines(BigInteger.valueOf(100));
            run = paragraph.createRun();
            run.addPicture(in, Document.PICTURE_TYPE_JPEG, "", Units.pixelToEMU((int) (width * scaling)), Units.pixelToEMU((int) (height * scaling)));
        }

        // 创建体检结果表格
        XWPFTable table = doc.createTable(item.size() + 1, 5);
        CTTblPr tblPr = table.getCTTbl().getTblPr();
        tblPr.getTblW().setType(STTblWidth.DXA);
        tblPr.getTblW().setW(BigInteger.valueOf(9850));

        // 表头行
        XWPFTableRow row = table.getRow(0);
        row.setHeight(500);
        List<XWPFTableCell> tableCells = row.getTableCells();
        String[] array = {"序号#800", "检查项目#2200", "检查结果#3000", "单位#1200", "参考值#1300",};
        for (int i = 0; i <= 4; i++) {
            XWPFTableCell cell = tableCells.get(i);
            cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
            cell.setColor("f0f0f0");
            paragraph = cell.getParagraphArray(0);
            if (i != 2) {
                paragraph.setAlignment(ParagraphAlignment.CENTER);
            } else {
                paragraph.setIndentationLeft(200);
            }
            run = paragraph.createRun();
            run.setFontSize(9);
            run.setFontFamily("Microsoft YaHei");
            String label = array[i].split("#")[0];
            int width = Integer.parseInt(array[i].split("#")[1]);
            run.setText(label);
            CTTcPr ctTcPr = cell.getCTTc().addNewTcPr();
            CTTblWidth ctTblWidth = ctTcPr.addNewTcW();
            ctTblWidth.setW(BigInteger.valueOf(width));
        }

        // 填充体检结果数据，支持检查结果换行显示
        for (int i = 0; i < item.size(); i++) {
            Map map = item.get(i);
            String name = MapUtil.getStr(map, "name");
            String value = MapUtil.getStr(map, "value");
            String unit = MapUtil.getStr(map, "unit");
            unit = unit == null ? "" : unit;
            String standard = MapUtil.getStr(map, "standard");
            standard = standard == null ? "" : standard;
            String[] temp = {i + 1 + "", name, value, unit, standard};
            row = table.getRow(i + 1);
            row.setHeight(500);
            tableCells = row.getTableCells();
            for (int j = 0; j < temp.length; j++) {
                XWPFTableCell cell = tableCells.get(j);
                cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
                paragraph = cell.getParagraphArray(0);
                run = paragraph.createRun();
                run.setFontSize(9);
                run.setFontFamily("Microsoft YaHei");
                if (j != 2) {
                    paragraph.setAlignment(ParagraphAlignment.CENTER);
                    run.setText(temp[j]);
                } else {
                    // 检查结果列特殊处理：支持换行显示
                    paragraph.setAlignment(ParagraphAlignment.LEFT);
                    paragraph.setIndentationLeft(200);
                    array = temp[j].split("#"); // 按#分割实现换行
                    for (int k = 0; k < array.length; k++) {
                        String one = array[k];
                        run.setText(one);
                        if (k < array.length - 1) {
                            run.addBreak(); // 添加换行
                        }
                    }
                }
            }
        }

        // 创建医生签名和日期段落
        paragraph = doc.createParagraph();
        run = paragraph.createRun();
        run.setFontSize(9);
        run.setFontFamily("Microsoft YaHei");
        run.setText("体检医生：" + doctor + "\t\t\t\t\t\t\t\t\t日期：" + date);
        ctp = paragraph.getCTP();
        ctpPr = ctp.addNewPPr();
        ctSpacing = ctpPr.addNewSpacing();
        ctSpacing.setBeforeLines(BigInteger.valueOf(100));
        ctSpacing.setAfterLines(BigInteger.valueOf(150));
    }

    /**
     * 核心方法：生成word文档对象。
     * @param map 生成时需要的数据
     * @return word文档对象
     * @throws Exception
     */
    public XWPFDocument createReport(HashMap map) throws Exception {
        // 创建Word文档对象
        XWPFDocument doc = new XWPFDocument();

        // 设置页面边距
        CTSectPr sectPr = doc.getDocument().getBody().addNewSectPr();
        CTPageMar pageMar = sectPr.addNewPgMar();
        pageMar.setLeft(BigInteger.valueOf(1200));
        pageMar.setRight(BigInteger.valueOf(1200));
        pageMar.setTop(BigInteger.valueOf(1000));
        pageMar.setBottom(BigInteger.valueOf(1000));

        // 设置页脚页码
        XWPFFooter footer = doc.createFooter(HeaderFooterType.DEFAULT);
        XWPFParagraph paragraph = footer.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        paragraph.getCTP().addNewFldSimple().setInstr("PAGE \\* MERGEFORMAT");
        paragraph.createRun();

        // 从参数map中提取数据
        String uuid = MapUtil.getStr(map, "uuid");
        String name = MapUtil.getStr(map, "name");
        String sex = MapUtil.getStr(map, "sex");
        String tel = MapUtil.getStr(map, "tel");
        String birthday = MapUtil.getStr(map, "birthday");
        String age = MapUtil.getStr(map, "age");
        String company = MapUtil.getStr(map, "company");
        String date = MapUtil.getStr(map, "date");
        String goods = MapUtil.getStr(map, "goods");
        List<Map> checkup = MapUtil.get(map, "checkup", List.class);
        List<Map> result = MapUtil.get(map, "result", List.class);

        // 构建参数对象
        HashMap param = new HashMap() {{
            put("uuid", uuid);
            put("item", new ArrayList<>() {{
                add(new HashMap<>() {{
                    put("label", "姓    名：");
                    put("value", name);
                }});
                add(new HashMap<>() {{
                    put("label", "性    别：");
                    put("value", sex);
                }});
                add(new HashMap<>() {{
                    put("label", "单    位：");
                    put("value", company);
                }});
                add(new HashMap<>() {{
                    put("label", "日    期：");
                    put("value", date);
                }});
            }});
            put("name", name);
            put("sex", sex);
            put("birthday", birthday);
            put("age", age);
            put("tel", tel);
            put("date", date);
            put("company", company);
            put("goods", goods);
        }};

        // 按顺序构建报告各部分
        createCover(doc, param);           // 封面页
        createWelcome(doc, param);         // 欢迎致辞
        createCustomerInfo(doc, param);    // 体检人信息
        createCheckup(doc, checkup);       // 体检内容

        // 按模板生成体检结果
        result.forEach(one -> {
            String template = MapUtil.getStr(one, "template");
            if ("模板1".equals(template)) {
                createCheckupResultByTemplate_1(doc, one);
            } else if ("模板2".equals(template)) {
                createCheckupResultByTemplate_2(doc, one);
            }
        });

        return doc;
    }

    // 工具的测试程序。
    public static void main(String[] args) {
        try {
            // 创建测试数据
            HashMap<String, Object> reportData = new HashMap<>();

            // 基本信息
            reportData.put("uuid", "1234567890");
            reportData.put("name", "张三");
            reportData.put("sex", "男");
            reportData.put("tel", "13800138000");
            reportData.put("birthday", "1985-05-15");
            reportData.put("age", "38");
            reportData.put("company", "天津科技有限公司");
            reportData.put("date", "2025-01-15");
            reportData.put("goods", "精英体检套餐");

            // 体检内容数据
            List<Map<String, String>> checkupList = new ArrayList<>();
            checkupList.add(new HashMap<String, String>() {{
                put("place", "内科");
                put("item", "心肺听诊、血压测量");
            }});
            checkupList.add(new HashMap<String, String>() {{
                put("place", "内科");
                put("item", "腹部触诊");
            }});
            checkupList.add(new HashMap<String, String>() {{
                put("place", "外科");
                put("item", "甲状腺检查");
            }});
            checkupList.add(new HashMap<String, String>() {{
                put("place", "眼科");
                put("item", "视力检查、眼底检查");
            }});
            reportData.put("checkup", checkupList);

            // 体检结果数据 - 模板1示例
            List<Map<String, Object>> resultList = new ArrayList<>();

            // 模板1结果
            Map<String, Object> template1Result = new HashMap<>();
            template1Result.put("place", "血常规");
            template1Result.put("doctorName", "李医生");
            template1Result.put("date", "2025-01-15");
            template1Result.put("template", "模板1");

            List<Map<String, String>> template1Items = new ArrayList<>();
            template1Items.add(new HashMap<String, String>() {{
                put("name", "白细胞计数");
                put("value", "6.5");
                put("unit", "×10^9/L");
                put("standard", "4.0-10.0");
            }});
            template1Items.add(new HashMap<String, String>() {{
                put("name", "红细胞计数");
                put("value", "4.8");
                put("unit", "×10^12/L");
                put("standard", "4.3-5.8");
            }});
            template1Items.add(new HashMap<String, String>() {{
                put("name", "血红蛋白");
                put("value", "145");
                put("unit", "g/L");
                put("standard", "130-175");
            }});
            template1Result.put("item", template1Items);
            resultList.add(template1Result);

            // 模板2结果
            Map<String, Object> template2Result = new HashMap<>();
            template2Result.put("place", "超声检查");
            template2Result.put("doctorName", "王医生");
            template2Result.put("date", "2025-01-15");
            template2Result.put("template", "模板2");
            template2Result.put("image", "https://p8.itc.cn/q_70/images03/20220805/24d5d42bf740446988d0a034770674e6.jpeg"); // 可选的图片URL

            List<Map<String, String>> template2Items = new ArrayList<>();
            template2Items.add(new HashMap<String, String>() {{
                put("name", "肝脏");
                put("value", "形态正常#大小正常#回声均匀");
                put("unit", "");
                put("standard", "");
            }});
            template2Items.add(new HashMap<String, String>() {{
                put("name", "胆囊");
                put("value", "壁光滑#腔内未见异常回声");
                put("unit", "");
                put("standard", "");
            }});
            template2Result.put("item", template2Items);
            resultList.add(template2Result);

            reportData.put("result", resultList);

            // 创建报告工具实例并生成文档
            CheckupReportUtil reportUtil = new CheckupReportUtil();
            XWPFDocument document = reportUtil.createReport(reportData);

            // 保存到D盘根目录
            String filePath = "d:/体检报告.doc";
            try (FileOutputStream out = new FileOutputStream(filePath)) {
                document.write(out);
                System.out.println("体检报告生成成功！文件保存位置：" + filePath);
            }

            // 关闭文档释放资源
            document.close();

        } catch (Exception e) {
            System.err.println("生成体检报告时发生错误：" + e.getMessage());
            e.printStackTrace();
        }
    }
}