# Kalewa Temperature Data Logger SDK (Demo-APP) V0.18c #

## License Declare ##

This program is free software; you can redistribute it and/or modify it under the terms 
of the GNU General Public License as published by the Free Software Foundation; either 
version 2 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
See the GNU General Public License for more details. 

You should have received a copy of the GNU General Public License along with this program; 
if not, write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.


## library 'nfc/Lib/kalewatdl_V1.1.aar' ##

 SDK contain a library 'kalewatdl_V1.1.aar' Version 0.3
 包含一个 库 'kalewatdl_V1.1.aar' Version 0.3
 
 Libaray contain a Class 'tdl_cmd'
 库中包含一个 Class 'tdl_cmd'
 
	 
### public variables: ###
公共变量：
	byte[] uid;            -- NFC UID number 7 bytes. NFC ID 码。
	int version_Maj;       -- Detected Logger frimware version Maj. 温度记录仪固化软件版本主。
	int version_Min;       -- Detected Logger firmware version Min. 温度记录仪固化软件版本次。
	int batt_vol;          -- Logger battery level. 温度记录仪内电池电压值。
	int interval;          -- Logger configured interval. 温度记录仪设置的测温间隔时间。
	long startEpoch;       -- Logger starting temperature measure date&time. 温度记录仪设置的起始测量日期和时间。
	int minimum;           -- Logger configured minimum threshold. 温度记录仪内设置的检查范围下限值。
	int maximum;           -- Logger configured maximum threshold. 温度记录仪内设置的检查范围上限值。
	int count;             -- Logger will stored temperature data number. 温度记录仪内储存的温度值数量。
	int valid;             -- Logger all stored data is in range? 温度记录仪内储存的温度值是否有超标？
	int maxCount;          -- Logger internal use, customer not need to acccess it. 温度记录仪内部使用。
	boolean MeasureFinish; -- Measure finish? 温度记录仪测温是否完成？
	int retrieved;         -- Retrieved temperature data. 从温度记录仪中提出的温度值数量。
	int[] tData;           -- All temperature data retrieved from Logger, unit 0.1 ℃. 从温度记录仪中提出的全部温度值数组，单位为0.1℃。
	                          If a data value is 851 (85.1℃) means it is abnormal. It is recommended that to take a normal value before or after to replace. 
							  如果数值为851（85.1℃）则该数据异常，建议用之前的或之后的正常值取代。
	
	final int UNKNOWN = 0;
	fianl int ADMIN = 1;
	final int NORMAL = 2;
	int authority;         -- After success identify, authority will euqal to ADMIN, NORMAL or NUKNOWN means Admin, Operator or unauthority.
	
	final int EMPTY = 0;
	final int FINISH = 1;
	final int PROGRESS = 2;
	
	final int PASSWORD_ERROR = 3;
	int error_message      -- call a method, if return false, it cantain detaisl of error message. for example, PASSWORD_ERROR indicate password wrong.
							  

### public methods: ###
公共方法：
	Logger identify with provided password and status receive.
	温度记录仪附带密码认证和记录仪内状态信息收集
	pass -- 8 bytes password in hex data. 8字节密码，十六进制数。
	return: true -- success,成功， false -- fail,失败
	if success: collected information in 'uid, version_Maj, int version_Min, batt_vol, interval,
				startEpoch, minimum, maximum, count, valid & MeasureFinish'.
	如果成功：收集的信息放入各变量中 'uid, version_Maj, int version_Min, batt_vol, interval,
				startEpoch, minimum, maximum, count, valid & MeasureFinish'。
	boolean identify(Intent intent, byte[] pass);
	
	After identify usccess, call it to get the TDL status.
	return EMPTY or FINISH or PROGRESS.
	int get_tag_status();
	
	Configure a new measure plan to logger.
	设置温度记录仪进行新的测试计划，输入各参赛。
	return: true -- success 成功, false -- fail 失败
	boolean applyConfig(Intent intent, int interval, int min, int max, int start, int running);
	
	Retrieve all temperature data
	导出温度记录仪中全部的温度数据
	return: true -- success 成功, false -- fail 失败
	if seccess: 'retrieved' contain total retrieved number, 'tData[]' contain all temperature data.
	如果成功：'retrieved' 存放导出的温度值数量, 'tData[]' 数组存放全部的温度值，存放每点温度值为实际温度值乘以10。
	boolean retrieve(Intent intent);
	
	Change password 更换密码
	AdminEn = 1, change admin password, AdminEn = 0, remine admin password unchange.
	AdminPass contain 8 bytes new Admin Password in Hex data.
	OperatorEn = 1, change operator password. OperatorEn =0, remine operator password unchange.
	OperatorPass cintain 8 bytes Operator password in Hex data.
	return: true -- success 成功, false -- fail 失败
	boolean changePassword(Intent intent, boolean AdminEn, byte[] AdminPass, boolean OperatorEn, byte[] OperatorPass);
	
	TDL password fix length 8 characrers. 
	Factory default: Admin - "#1234567", Operator - "12345678"
	
	DEMO APP Password change process steps:
	1. Running APP.
	2. confirm the main UI "Password EditText" contain Admin password.
	3. Click INPUT, will popup a "New Password Input" dialog box。
	4. Tick "Admin password modify enable" if want to change admin password, and keyin 8 characters new admin passwird in Admin Edittext box.
	5. Tick "Operator password modify enable" if want to change operator password, and keyin 8 characters new operator passwird in Operator Edittext box.
	6. Can change any one password or change both passwords. Click eye image can tiggle password visibility.
	7. Click "ENSURE" to confirm and exit "New Password Input" dialog. The new passwords will be displayed on main UI TextView for you double check and record them!!!
	8. Tap a TDL, Admin will display on main UI TextView.
	9. hold the TDL,keep communication with Android device stable. Click "CHANGE". After a shot while, the "Password change success." display. Password change done.
	
	
Details about the SDK use guide please refer to the DEMO APP program.
SDK 详细的使用方法请参考本样板APP程序。

TDL -- Temperature Data Logger
## The End ##