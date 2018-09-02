import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class writer {
	private String path = new String();
	private FileWriter file;

	public writer(String Path) throws IOException {
		path = Path;
		initWriter(path);
	}

	public writer() throws IOException {
		path = "default.txt";
		initWriter(path);
	}

	private void initWriter(String Path) throws IOException {
		file = new FileWriter(new File(Path));
	}
	
	private void initWriter(String Path, boolean reopen) throws IOException {
		file = new FileWriter(new File(Path), reopen);
	}

	public void newLine() throws IOException {
		file.write(System.lineSeparator());
	}

	public void empty(int width) throws IOException {
		for (int i = 0; i < width; i++) {
			newLine();
		}
	}

	public void line(int width, int padding) throws IOException {
		empty(padding + 1);
		for (int i = 0; i < width; i++) {
			file.write("-------------------------");
			newLine();
		}
		empty(padding);
	}
	
	public void line() throws IOException {
		file.write("-------------------------");
		newLine();
	}
	
	public void line(char symbol, int width, int padding) throws IOException {
		empty(padding + 1);
		for (int i = 0; i < width; i++) {
			file.write("-------------------------");
			newLine();
		}
		empty(padding);
	}
	
	public void line(String symbol, int length) throws IOException {
		String line_string = new String();
		for (int i = 0; i < length; i++) {
			line_string = line_string.concat(symbol);
		}
		file.write(line_string);
		newLine();
	}
	
	public void write(String text) throws IOException {
		file.write(text);
	}
	
	public void write(String text, boolean newline) throws IOException {
		file.write(text);
		if(newline)
			newLine();
	}
	
	public void write(String text, int offset, int length) throws IOException {
		file.write(text, offset, length);;
	}
	
	public void done() throws IOException {
		file.flush();
		file.close();
		console.log("\nThe File has been saved at: "+path);
	}
	
	public void reopen() throws IOException {
		initWriter(path, true);
		console.log("The File at: "+path+" has ben re-opened.");
	}

}
