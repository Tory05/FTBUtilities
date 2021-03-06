package com.feed_the_beast.ftbu.world.backups;

import com.feed_the_beast.ftbl.lib.util.FileUtils;
import com.feed_the_beast.ftbl.lib.util.StringUtils;
import com.feed_the_beast.ftbu.FTBUFinals;
import com.feed_the_beast.ftbu.api.FTBULang;
import com.feed_the_beast.ftbu.config.FTBUConfigBackups;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ThreadBackup extends Thread
{
	public boolean isDone = false;
	private final File src0;
	private final String customName;

	public ThreadBackup(File w, String s)
	{
		src0 = w;
		customName = s;
		setPriority(7);
	}

	public static void doBackup(File src, String customName)
	{
		Calendar time = Calendar.getInstance();
		File dstFile = null;
		boolean success = false;
		StringBuilder out = new StringBuilder();

		if (customName.isEmpty())
		{
			appendNum(out, time.get(Calendar.YEAR), '-');
			appendNum(out, time.get(Calendar.MONTH) + 1, '-');
			appendNum(out, time.get(Calendar.DAY_OF_MONTH), '-');
			appendNum(out, time.get(Calendar.HOUR_OF_DAY), '-');
			appendNum(out, time.get(Calendar.MINUTE), '-');
			appendNum(out, time.get(Calendar.SECOND), '\0');
		}
		else
		{
			out.append(customName);
		}

		try
		{
			List<File> files = FileUtils.listAll(src);
			int allFiles = files.size();

			FTBUFinals.LOGGER.info("Backing up " + files.size() + " files..."); //LANG

			if (FTBUConfigBackups.COMPRESSION_LEVEL.getInt() > 0)
			{
				out.append(".zip");
				dstFile = FileUtils.newFile(new File(Backups.INSTANCE.backupsFolder, out.toString()));

				long start = System.currentTimeMillis();

				ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(dstFile));
				//zos.setLevel(9);
				zos.setLevel(FTBUConfigBackups.COMPRESSION_LEVEL.getInt());

				long logMillis = System.currentTimeMillis() + 5000L;

				byte[] buffer = new byte[4096];

				FTBUFinals.LOGGER.info("Compressing " + allFiles + " files!"); //LANG

				for (int i = 0; i < allFiles; i++)
				{
					try
					{
						File file = files.get(i);
						String filePath = file.getAbsolutePath();
						ZipEntry ze = new ZipEntry(src.getName() + File.separator + filePath.substring(src.getAbsolutePath().length() + 1, filePath.length()));

						long millis = System.currentTimeMillis();

						if (i == 0 || millis > logMillis || i == allFiles - 1)
						{
							logMillis = millis + 5000L;
							FTBUFinals.LOGGER.info("[" + i + " | " + StringUtils.formatDouble((i / (double) allFiles) * 100D) + "%]: " + ze.getName());
						}

						zos.putNextEntry(ze);
						FileInputStream fis = new FileInputStream(file);

						int len;
						while ((len = fis.read(buffer)) > 0)
						{
							zos.write(buffer, 0, len);
						}
						zos.closeEntry();
						fis.close();
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}

				zos.close();

				FTBUFinals.LOGGER.info("Done compressing in " + getDoneTime(start) + " seconds (" + FileUtils.getSizeS(dstFile) + ")!"); //LANG
			}
			else
			{
				out.append('/');
				out.append(src.getName());
				dstFile = new File(Backups.INSTANCE.backupsFolder, out.toString());
				dstFile.mkdirs();

				String dstPath = dstFile.getAbsolutePath() + File.separator;
				String srcPath = src.getAbsolutePath();

				long logMillis = System.currentTimeMillis() + 2000L;

				for (int i = 0; i < allFiles; i++)
				{
					try
					{
						File file = files.get(i);

						long millis = System.currentTimeMillis();

						if (i == 0 || millis > logMillis || i == allFiles - 1)
						{
							logMillis = millis + 2000L;
							FTBUFinals.LOGGER.info("[" + i + " | " + StringUtils.formatDouble((i / (double) allFiles) * 100D) + "%]: " + file.getName()); //LANG
						}

						File dst1 = new File(dstPath + (file.getAbsolutePath().replace(srcPath, "")));
						FileUtils.copyFile(file, dst1);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}

			FTBUFinals.LOGGER.info("Created " + dstFile.getAbsolutePath() + " from " + src.getAbsolutePath());
			success = true;

			if (!FTBUConfigBackups.SILENT.getBoolean())
			{
				if (FTBUConfigBackups.DISPLAY_FILE_SIZE.getBoolean())
				{
					String sizeB = FileUtils.getSizeS(dstFile);
					String sizeT = FileUtils.getSizeS(Backups.INSTANCE.backupsFolder);
					Backups.notifyAll(FTBULang.BACKUP_END_2.textComponent(getDoneTime(time.getTimeInMillis()), (sizeB.equals(sizeT) ? sizeB : (sizeB + " | " + sizeT))), false);
				}
				else
				{
					Backups.notifyAll(FTBULang.BACKUP_END_1.textComponent(getDoneTime(time.getTimeInMillis())), false);
				}
			}
		}
		catch (Exception ex)
		{
			if (!FTBUConfigBackups.SILENT.getBoolean())
			{
				Backups.notifyAll(FTBULang.BACKUP_FAIL.textComponent(ex.getClass().getName()), true);
			}

			ex.printStackTrace();
			if (dstFile != null)
			{
				FileUtils.delete(dstFile);
			}
		}

		Backups.INSTANCE.backups.add(new Backup(time.getTimeInMillis(), out.toString().replace('\\', '/'), Backups.INSTANCE.getLastIndex() + 1, success));
		Backups.INSTANCE.cleanupAndSave();
	}

	private static String getDoneTime(long l)
	{
		l = System.currentTimeMillis() - l;

		if (l < 1000L)
		{
			return l + "ms";
		}

		return StringUtils.getTimeString(l);
	}

	private static void appendNum(StringBuilder sb, int num, char c)
	{
		if (num < 10)
		{
			sb.append('0');
		}
		sb.append(num);
		if (c != '\0')
		{
			sb.append(c);
		}
	}

	@Override
	public void run()
	{
		isDone = false;
		doBackup(src0, customName);
		isDone = true;
	}
}