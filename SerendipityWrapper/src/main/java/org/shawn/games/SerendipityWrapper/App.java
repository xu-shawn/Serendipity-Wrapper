package org.shawn.games.SerendipityWrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Scanner;

public class App
{
	public static void main(String[] args)
	{
		String jarFileName = "Serendipity-1.0.jar";

		File currentDir;

		try
		{
			currentDir = new File(App.class.getProtectionDomain().getCodeSource().getLocation().toURI())
					.getParentFile();
			System.out.println("Launching " + jarFileName + " from " + currentDir);
		}
		catch (URISyntaxException e)
		{
			System.out.println("Error: Could not determine the directory of the executing JAR.");
			e.printStackTrace();
			return;
		}

		File jarFile = new File(currentDir, jarFileName);

		if (!jarFile.exists() || !jarFile.isFile())
		{
			System.out.println("Error: " + jarFileName + " not found in the current directory.");
			return;
		}

		try
		{
			ProcessBuilder pb = new ProcessBuilder("java", "--add-modules", "jdk.incubator.vector", "-jar",
					jarFile.getAbsolutePath());

			Process process = pb.start();

			OutputStream processStdin = process.getOutputStream();
			InputStream processStdout = process.getInputStream();
			InputStream processStderr = process.getErrorStream();

			Thread stdinToProcess = new Thread(() -> pipeStdinToProcess(processStdin));
			Thread processToStdout = new Thread(() -> pipeProcessToStdout(processStdout));
			Thread processToStderr = new Thread(() -> pipeProcessToStderr(processStderr));

			stdinToProcess.start();
			processToStdout.start();
			processToStderr.start();

			process.waitFor();

			System.exit(0);
		}
		catch (IOException | InterruptedException e)
		{
			System.err.println("Error executing JAR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static void pipeStdinToProcess(OutputStream processStdin)
	{
		try (Scanner scanner = new Scanner(System.in))
		{
			while (scanner.hasNextLine())
			{
				String line = scanner.nextLine() + System.lineSeparator();
				processStdin.write(line.getBytes());
				processStdin.flush();
			}
			processStdin.close();
		}
		catch (IOException e)
		{
			System.err.println("Error piping stdin to process: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static void pipeProcessToStdout(InputStream processStdout)
	{
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(processStdout)))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				System.out.println(line);
			}
		}
		catch (IOException e)
		{
			System.err.println("Error piping process stdout: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static void pipeProcessToStderr(InputStream processStderr)
	{
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(processStderr)))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				System.err.println(line);
			}
		}
		catch (IOException e)
		{
			System.err.println("Error piping process stderr: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
