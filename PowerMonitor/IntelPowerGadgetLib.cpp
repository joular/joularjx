/*
Copyright (c) (2013) Intel Corporation All Rights Reserved.

The source code, information and material ("Material") contained herein is owned by Intel Corporation or its suppliers or licensors, and title to such Material remains with Intel Corporation or its suppliers or licensors. The Material contains proprietary information of Intel or its suppliers and licensors. The Material is protected by worldwide copyright laws and treaty provisions. No part of the Material may be used, copied, reproduced, modified, published, uploaded, posted, transmitted, distributed or disclosed in any way without Intel's prior express written permission. No license under any patent, copyright or other intellectual property rights in the Material is granted to or conferred upon you, either expressly, by implication, inducement, estoppel or otherwise. Any license under such intellectual property rights must be express and approved by Intel in writing.


Include any supplier copyright notices as supplier requires Intel to use.

Include supplier trademarks or logos as supplier requires Intel to use, preceded by an asterisk. An asterisked footnote can be added as follows: *Third Party trademarks are the property of their respective owners.

Unless otherwise agreed by Intel in writing, you may not remove or alter this notice or any other notice embedded in Materials by Intel or Intel's suppliers or licensors in any way.
*/

#define _CRT_SECURE_NO_WARNINGS

#include <iostream>

#include <SDKDDKVer.h>
#include <stdio.h>
#include <tchar.h>
#include <Windows.h>
#include <string>
#include <vector>

#include "IntelPowerGadgetLib.h"

using namespace std;

wstring g_lastError;
HMODULE g_hModule = NULL;

static bool split(const wstring& s, wstring& path)
{
	bool bResult = false;
	vector<wstring> output;

	wstring::size_type prev_pos = 0, pos = 0;

	while ((pos = s.find(L';', pos)) != wstring::npos)
	{
		wstring substring(s.substr(prev_pos, pos - prev_pos));
		if (substring.find(L"Power Gadget 2.") != wstring::npos)
		{
			path = substring;
			bResult = true;
			break;
		}
		prev_pos = ++pos;
	}

	if (!bResult)
	{
		wstring substring(s.substr(prev_pos, pos - prev_pos));

		if (substring.find(L"Power Gadget 2.") != wstring::npos)
		{
			path = substring;
			bResult = true;
		}
	}

	if (bResult)
	{
		basic_string <char>::size_type pos = path.rfind(L" ");
		wstring version = path.substr(pos + 1, path.length());
		double fVer = _wtof(version.c_str());
		if (fVer > 2.6)
			bResult = true;
	}

	return bResult;
}

static bool GetLibraryLocation(wstring& strLocation)
{
	TCHAR* pszPath = _wgetenv(L"IPG_Dir");
	if (pszPath == NULL || wcslen(pszPath) == 0)
		return false;

	TCHAR* pszVersion = _wgetenv(L"IPG_Ver");
	if (pszVersion == NULL || wcslen(pszVersion) == 0)
		return false;

	int version = _wtof(pszVersion) * 100;
	if (version >= 270)
	{

#if _M_X64
		strLocation = wstring(pszPath) + L"\\EnergyLib64.dll";
#else
		strLocation = wstring(pszPath) + L"\\EnergyLib32.dll";
#endif
		return true;
	}
	else
		return false;
}

CIntelPowerGadgetLib::CIntelPowerGadgetLib(void) :
	pInitialize(NULL),
	pGetNumNodes(NULL),
	pGetMsrName(NULL),
	pGetMsrFunc(NULL),
	pGetIAFrequency(NULL),
	pGetGTFrequency(NULL),
	pGetTDP(NULL),
	pGetMaxTemperature(NULL),
	pGetTemperature(NULL),
	pReadSample(NULL),
	pGetSysTime(NULL),
	pGetTimeInterval(NULL),
	pGetBaseFrequency(NULL),
	pGetPowerData(NULL),
	pStartLog(NULL),
	pStopLog(NULL),
	pGetNumMsrs(NULL),
	pIsGTAvailable(NULL)
{
	wstring strLocation;
	if (GetLibraryLocation(strLocation) == false)
	{
		g_lastError = L"Intel Power Gadget 2.7 or higher not found. If unsure, check if the path is in the user's path environment variable";
		return;
	}

	g_hModule = LoadLibrary(strLocation.c_str());
	if (g_hModule == NULL)
	{
		g_lastError = L"LoadLibrary failed on " + strLocation;
		return;
	}

	pInitialize = (IPGInitialize)GetProcAddress(g_hModule, "IntelEnergyLibInitialize");
	pGetNumNodes = (IPGGetNumNodes)GetProcAddress(g_hModule, "GetNumNodes");
	pGetMsrName = (IPGGetMsrName)GetProcAddress(g_hModule, "GetMsrName");
	pGetMsrFunc = (IPGGetMsrFunc)GetProcAddress(g_hModule, "GetMsrFunc");
	pGetIAFrequency = (IPGGetIAFrequency)GetProcAddress(g_hModule, "GetIAFrequency");
	pGetGTFrequency = (IPGGetGTFrequency)GetProcAddress(g_hModule, "GetGTFrequency");
	pGetTDP = (IPGGetTDP)GetProcAddress(g_hModule, "GetTDP");
	pGetMaxTemperature = (IPGGetMaxTemperature)GetProcAddress(g_hModule, "GetMaxTemperature");
	pGetTemperature = (IPGGetTemperature)GetProcAddress(g_hModule, "GetTemperature");
	pReadSample = (IPGReadSample)GetProcAddress(g_hModule, "ReadSample");
	pGetSysTime = (IPGGetSysTime)GetProcAddress(g_hModule, "GetSysTime");
	pGetTimeInterval = (IPGGetTimeInterval)GetProcAddress(g_hModule, "GetTimeInterval");
	pGetBaseFrequency = (IPGGetBaseFrequency)GetProcAddress(g_hModule, "GetBaseFrequency");
	pGetPowerData = (IPGGetPowerData)GetProcAddress(g_hModule, "GetPowerData");
	pStartLog = (IPGStartLog)GetProcAddress(g_hModule, "StartLog");
	pStopLog = (IPGStopLog)GetProcAddress(g_hModule, "StopLog");
	pGetNumMsrs = (IPGGetNumMsrs)GetProcAddress(g_hModule, "GetNumMsrs");
	pIsGTAvailable = (IPGIsGTAvailable)GetProcAddress(g_hModule, "IsGTAvailable");
}


CIntelPowerGadgetLib::~CIntelPowerGadgetLib(void)
{
	if (g_hModule != NULL)
		FreeLibrary(g_hModule);
}



wstring CIntelPowerGadgetLib::GetLastError()
{
	return g_lastError;
}

bool CIntelPowerGadgetLib::IntelEnergyLibInitialize(void)
{
	if (pInitialize == NULL)
		return false;

	bool bSuccess = pInitialize();
	if (!bSuccess)
	{
		g_lastError = L"Initializing the energy library failed";
		return false;
	}

	return true;
}


bool CIntelPowerGadgetLib::GetNumNodes(int* nNodes)
{
	return pGetNumNodes(nNodes);
}

bool CIntelPowerGadgetLib::GetNumMsrs(int* nMsrs)
{
	return pGetNumMsrs(nMsrs);
}

bool CIntelPowerGadgetLib::GetMsrName(int iMsr, wchar_t* pszName)
{
	return pGetMsrName(iMsr, pszName);
}

bool CIntelPowerGadgetLib::GetMsrFunc(int iMsr, int* funcID)
{
	return pGetMsrFunc(iMsr, funcID);
}

bool CIntelPowerGadgetLib::GetIAFrequency(int iNode, int* freqInMHz)
{
	return pGetIAFrequency(iNode, freqInMHz);
}

bool CIntelPowerGadgetLib::GetGTFrequency(int* freq)
{
	return pGetGTFrequency(freq);
}

bool CIntelPowerGadgetLib::GetTDP(int iNode, double* TDP)
{
	return pGetTDP(iNode, TDP);
}

bool CIntelPowerGadgetLib::GetMaxTemperature(int iNode, int* degreeC)
{
	return pGetMaxTemperature(iNode, degreeC);
}

bool CIntelPowerGadgetLib::GetTemperature(int iNode, int* degreeC)
{
	return pGetTemperature(iNode, degreeC);
}

bool CIntelPowerGadgetLib::ReadSample()
{
	bool bSuccess = pReadSample();
	if (bSuccess == false)
		g_lastError = L"MSR overflowed. You can safely discard this sample";
	return bSuccess;
}

bool CIntelPowerGadgetLib::GetSysTime(SYSTEMTIME* sysTime)
{
	return pGetSysTime(sysTime);
}

bool CIntelPowerGadgetLib::GetTimeInterval(double* offset)
{
	return pGetTimeInterval(offset);
}

bool CIntelPowerGadgetLib::GetBaseFrequency(int iNode, double* baseFrequency)
{
	return pGetBaseFrequency(iNode, baseFrequency);
}

bool CIntelPowerGadgetLib::GetPowerData(int iNode, int iMSR, double* results, int* nResult)
{
	return pGetPowerData(iNode, iMSR, results, nResult);
}

bool CIntelPowerGadgetLib::StartLog(wchar_t* szFilename)
{
	return pStartLog(szFilename);
}

bool CIntelPowerGadgetLib::StopLog()
{
	return pStopLog();
}

bool CIntelPowerGadgetLib::IsGTAvailable()
{
	return pIsGTAvailable();
}