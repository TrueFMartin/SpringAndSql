package com.github.truefmartin.api;

import com.github.truefmartin.Main;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
public class BuildController {

//	@GetMapping("/build")
//	public String greeting() {
//		Main.Build(new String[]{"files", "outfiles"});
//		return files();
//	}

	public static String files() {
		File folder = new File("./outfiles"); // Replace with your directory path
		File[] listOfFiles = folder.listFiles();
		StringBuilder sb = new StringBuilder();
		if (listOfFiles != null) {
			for (File file : listOfFiles) {
				if (file.isFile()) {
					sb.append("file: ").append(file.getName()).append('\n');
				} else if (file.isDirectory()) {
					sb.append("dir: ").append(file.getName()).append('\n');
				}
			}
		}
		return sb.toString();
	}
}
