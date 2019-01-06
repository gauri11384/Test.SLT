
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/FileDownloadServlet"})
public class FileDownloadServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Need to pass below query string parameters 
        String refno = request.getParameter("refno");
        String fileName = request.getParameter("filename");
        
        // Get this from the web.xml using ServletContext
        String serverFolderPath = "C:\\Compello\\TestFiles"; 

        String errorMessage = "";
        if (refno == null || refno.isEmpty() || fileName == null || fileName.isEmpty()) {
            errorMessage = "Ref number & file name required!";
        } else {
            // Reads input file from an absolute path and send to client side
            String filePath = serverFolderPath + "\\" + refno + "\\" + fileName;
            errorMessage = sendFile(filePath, response);
        }

        if (!errorMessage.isEmpty()) {
            response.setContentType("text/html;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.println("<!DOCTYPE html>");
                out.println("<html>");
                out.println("<head>");
                out.println("<title>File Download</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<h3>Download error - " + errorMessage + "</h3>");
                out.println("</body>");
                out.println("</html>");
            }
        }
    }

    private String sendFile(String filePath, HttpServletResponse response) {
        String errorMessage = "";
        File downloadFile = new File(filePath);
        OutputStream outStream;
        try (FileInputStream inStream = new FileInputStream(downloadFile)) {
            ServletContext context = getServletContext();
            // gets MIME type of the file
            String mimeType = context.getMimeType(filePath);
            if (mimeType == null) {
                // set to binary type if MIME mapping not found
                mimeType = "application/octet-stream";
            }
            // modifies response
            response.setContentType(mimeType);
            response.setContentLength((int) downloadFile.length());
            // forces download
            String headerKey = "Content-Disposition";
            String headerValue = String.format("attachment; filename=\"%s\"", downloadFile.getName());
            response.setHeader(headerKey, headerValue);
            // obtains response's output stream
            outStream = response.getOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
            outStream.close();
        } catch (FileNotFoundException ex) {
            errorMessage = "File not found: " + filePath;
        } catch (IOException ex) {
            Logger.getLogger(FileDownloadServlet.class.getName()).log(Level.SEVERE, null, ex);
            errorMessage = "IO error on reading file: " + filePath;
        }

        return errorMessage;
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
