package com.alex.web;

import com.alex.utils.MyFileReader;
import com.alex.utils.Utils;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@WebServlet(name = "DownloadFile", urlPatterns = "/download")
@MultipartConfig
public class DownloadFile extends HttpServlet {

    private Logger log = Logger.getLogger(DownloadFile.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.info("/download:POST");

        Part filePart = request.getPart("file"); // Retrieves <input type="file" name="file">
//        String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString(); // MSIE fix.
        String fileName = filePart.getSubmittedFileName();
        log.info("filename == " + fileName);

        // Попытаемся никуда не сохранять файл, а распарсить его на ходу
        try (InputStream fileContent = filePart.getInputStream()) {
            int i;
            List<Byte> arr = new ArrayList<>();
            Pattern p = Pattern.compile("[a-zA-Zа-яА-Я]");
            Map<String, Integer> hashMap = new HashMap<>();
            while ((i = fileContent.read()) != -1) {
                if (p.matcher(String.valueOf((char) i)).matches()) { // Собираем слово
                    arr.add((byte) i);
                } else { // Слово собрано
                    byte[] temp = new byte[arr.size()]; // TODO 2 байта на символ??

                    AtomicInteger k = new AtomicInteger();
                    arr.forEach(b->{temp[k.getAndIncrement()] = b;}); // TODO Если эта штука работает параллельно, то слово может превратиться в кашу

                    String word = new String(temp, "UTF-8");
                    arr.clear();
                    if (hashMap.containsKey(word)) {
                        hashMap.put(word, hashMap.get(word) + 1);
                    } else {
                        hashMap.put(word, 1);
                    }
                }
            }
            MyFileReader.pasteIntoDB(fileName, hashMap);
        }

        request.setAttribute("count", Utils.getCountRowDB());
        request.getRequestDispatcher("index.jsp").forward(request, response);
    }
}
