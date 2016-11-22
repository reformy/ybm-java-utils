package com.kwazylabs.utils.filedownload;

import java.io.File;
import java.io.Serializable;

class FileInfo implements Serializable
{
	File file;
	long expirationTime;
}
