/*
Copyright (c) (2013) Intel Corporation All Rights Reserved.

The source code, information and material ("Material") contained herein is owned by Intel Corporation or its suppliers or licensors, and title to such Material remains with Intel Corporation or its suppliers or licensors. The Material contains proprietary information of Intel or its suppliers and licensors. The Material is protected by worldwide copyright laws and treaty provisions. No part of the Material may be used, copied, reproduced, modified, published, uploaded, posted, transmitted, distributed or disclosed in any way without Intel's prior express written permission. No license under any patent, copyright or other intellectual property rights in the Material is granted to or conferred upon you, either expressly, by implication, inducement, estoppel or otherwise. Any license under such intellectual property rights must be express and approved by Intel in writing.


Include any supplier copyright notices as supplier requires Intel to use.

Include supplier trademarks or logos as supplier requires Intel to use, preceded by an asterisk. An asterisked footnote can be added as follows: *Third Party trademarks are the property of their respective owners.

Unless otherwise agreed by Intel in writing, you may not remove or alter this notice or any other notice embedded in Materials by Intel or Intel's suppliers or licensors in any way.
*/


#pragma once
#include <Windows.h>
#include <string>

using namespace std;

typedef bool (*IPGInitialize) ();
typedef bool (*IPGGetNumNodes) (int* nNodes);
typedef bool (*IPGGetNumMsrs) (int* nMsr);
typedef bool (*IPGGetMsrName) (int iMsr, wchar_t* szName);
typedef bool (*IPGGetMsrFunc) (int iMsr, int* funcID);
typedef bool (*IPGGetIAFrequency) (int iNode, int* freqInMHz);
typedef bool (*IPGGetGTFrequency) (int* freq);
typedef bool (*IPGGetTDP) (int iNode, double* TDP);
typedef bool (*IPGGetMaxTemperature) (int iNode, int* degreeC);
typedef bool (*IPGGetTemperature) (int iNode, int* degreeC);
typedef bool (*IPGReadSample) ();
typedef bool (*IPGGetSysTime) (SYSTEMTIME* pSysTime);
typedef bool (*IPGGetTimeInterval) (double* offset);
typedef bool (*IPGGetBaseFrequency) (int iNode, double* baseFrequency);
typedef bool (*IPGGetPowerData) (int iNode, int iMSR, double* result, int* nResult);
typedef bool (*IPGStartLog) (wchar_t* szFileName);
typedef bool (*IPGStopLog) ();
typedef bool (*IPGIsGTAvailable) ();

class CIntelPowerGadgetLib
{
public:
	CIntelPowerGadgetLib(void);
	~CIntelPowerGadgetLib(void);

	bool IntelEnergyLibInitialize(void);
	bool GetNumNodes(int* nNodes);
	bool GetNumMsrs(int* nMsrs);
	bool GetMsrName(int iMsr, wchar_t* szName);
	bool GetMsrFunc(int iMsr, int* funcID);
	bool GetIAFrequency(int iNode, int* freqInMHz);
	bool GetGTFrequency(int* freq);
	bool GetTDP(int iNode, double* TDP);
	bool GetMaxTemperature(int iNode, int* degreeC);
	bool GetTemperature(int iNode, int* degreeC);
	bool ReadSample();
	bool GetSysTime(SYSTEMTIME* sysTime);
	bool GetTimeInterval(double* offset);
	bool GetBaseFrequency(int iNode, double* baseFrequency);
	bool GetPowerData(int iNode, int iMSR, double* results, int* nResult);
	bool StartLog(wchar_t* szFilename);
	bool StopLog();
	bool IsGTAvailable();
	wstring GetLastError();

private:
	IPGInitialize pInitialize;
	IPGGetNumNodes pGetNumNodes;
	IPGGetNumMsrs pGetNumMsrs;
	IPGGetMsrName pGetMsrName;
	IPGGetMsrFunc pGetMsrFunc;
	IPGGetIAFrequency pGetIAFrequency;
	IPGGetGTFrequency pGetGTFrequency;
	IPGGetTDP pGetTDP;
	IPGGetMaxTemperature pGetMaxTemperature;
	IPGGetTemperature pGetTemperature;
	IPGReadSample pReadSample;
	IPGGetSysTime pGetSysTime;
	IPGGetTimeInterval pGetTimeInterval;
	IPGGetBaseFrequency pGetBaseFrequency;
	IPGGetPowerData pGetPowerData;
	IPGStartLog pStartLog;
	IPGStopLog pStopLog;
	IPGIsGTAvailable pIsGTAvailable;
};

