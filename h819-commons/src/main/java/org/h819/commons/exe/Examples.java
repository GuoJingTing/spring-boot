package org.h819.commons.exe;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.h819.commons.MyConstants;
import org.h819.commons.MyExecUtils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Description : TODO()
 * User: h819
 * Date: 2016/4/1
 * Time: 13:22
 * To change this template use File | Settings | File Templates.
 */
public class Examples {


    /**
     * 用这个简单的例子吧
     * 需要注意的是，如果命令行中，exe 文件所在路径有空给，需要用双引号引起来，如
     * echo show databases; | "C:\Program Files\MySQL\MySQL Server 5.7\bin\mysql.exe" -h localhost -u root -p123456 -P3306 > "D:\02\databaselist.txt"
     * 个别的命令行，不能包含空格，即使是用引号引起来也不行，如 mysqldump
     */


    /**
     * 执行指定命令
     *
     * @param command 待执行的命令行字符串
     * @return 执行是否成功
     */
    private boolean execWindowsCommand(String command) {
        CommandLine cmd = new CommandLine("cmd.exe ");
        cmd.addArgument("/c");
        cmd.addArgument(command, false);
        try {
            new DefaultExecutor().execute(cmd);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    /**
     * pdf2swf example
     */
    @Deprecated
    private void pdf2Swf() {

        String srcPdf = "d:\\test\\src.pdf";
        String descSwf = "d:\\test\\src%.swf";
        String pdf2swfComandPath = "E:\\program\\flexpaper\\swftools-2013-04-09-1007\\pdf2swf.exe";
        String xpdflanguagePath = "E:\\program\\flexpaper\\xpdf-chinese-simplified";

        List<ExecParameter> list = new ArrayList<ExecParameter>();
        list.add(new ExecParameter("", MyConstants.ExecEmptyValue)); // 只有 key ，没有 value
        list.add(new ExecParameter("-o", descSwf));
        list.add(new ExecParameter("-f", MyConstants.ExecEmptyValue));
        list.add(new ExecParameter("-T", "9"));
        list.add(new ExecParameter("-t", MyConstants.ExecEmptyValue));
        list.add(new ExecParameter("-j=100", MyConstants.ExecEmptyValue));
        list.add(new ExecParameter("-s", "languagedir=" + xpdflanguagePath)); //-s 参数用了两次
        list.add(new ExecParameter("-s", "storeallcharacters"));  //-s 参数用了两次
        //list.add(new ExecParameter("-s", "multiply=2")); // Convert graphics to bitmaps 大文件时有助于减小文件体积，转换速度会变慢
        // list.add(new ExecParameter("-s", "poly2bitmap")); // Convert graphics to bitmaps 大文件时有助于减小文件体积 ，转换速度会变慢
        list.add(new ExecParameter("-s", "protect")); //add a "protect" tag to the file, to prevent loading in the Flash editor
        MyExecUtils.exec(Paths.get(pdf2swfComandPath), list, 1);
    }

    /**
     * pdf de example
     */
    @Deprecated
    private void pdfdecrypt() {

        String srcPdf = "d:\\test\\src.pdf";
        String descPdf = "d:\\test\\desc.pdf";
        String pdf2swfComandPath = "D:\\swap\\local\\java_jar_source_temp\\pdfdecrypt.exe";
        String userPassword = "upass";
        String ownerPassword = "opass";

        List<ExecParameter> list = new ArrayList<ExecParameter>();
        list.add(new ExecParameter("-i", srcPdf)); // 只有 key ，没有 value
        list.add(new ExecParameter("-o", descPdf));
        list.add(new ExecParameter("-u", userPassword));
        list.add(new ExecParameter("-w", ownerPassword));
        MyExecUtils.exec(Paths.get(pdf2swfComandPath), list, 1);
    }


}
