#!/usr/bin/python
import string,re,sys
if __name__ == '__main__':
	if (len(sys.argv) < 2):
		print "please specify the input file"
		sys.exit(0)
	resF = open("res_" + sys.argv[1], "w")
	logF = file( sys.argv[1] )
	timeT = [[],[],[],[],[]]
	sms = [0] * 5
	shh = [0] * 5
	smm = [0] * 5
	sss = [0] * 5
	ems = [0] * 5
	ehh = [0] * 5
	emm = [0] * 5
	ess = [0] * 5
	diff = 0
	idx = {'---av_read_frame':0, '---avcodec_decode_video2':1,
		'---sws_scale':2, '---draw_on_canvas':3, '---av_seek_frame':4}
	for aLine in logF:
		if ("ST" in aLine or "ED" in aLine):
			token = re.split(": ", aLine)[0] #get the part until the tag
			#print token
			token = re.split(" ", token)[1]	 #get the time
			#print token
			ts = re.split("\.", token)	#split xx:xx:xx and .xxx in time
			#print ts[0]
			funName = re.split(": ", aLine)[1]
			funName = re.split(" ", funName)[0]
			#print funName
			if ("ST" in aLine):
				sms[idx[funName]] = int(ts[1])
				ts = re.split(":", ts[0])
				shh[idx[funName]] = int(ts[0])
				smm[idx[funName]] = int(ts[1])
				sss[idx[funName]] = int(ts[2])
				#print sms + " " + shh + " " + smm + " " + sss 
			elif ("ED" in aLine):
				if (shh[idx[funName]] == 0 and smm[idx[funName]] == 0 and sss[idx[funName]] == 0 and sms[idx[funName]] == 0):
					#the start time is not captured, skip this record
					continue	
				ems[idx[funName]] = int(ts[1])
				ts = re.split(":", ts[0])
				ehh[idx[funName]] = int(ts[0])
				emm[idx[funName]] = int(ts[1])
				ess[idx[funName]] = int(ts[2])
				#print ems + " " + ehh + " " + emm + " " + ess 
				if (shh[idx[funName]] > ehh[idx[funName]]):
					ehh[idx[funName]] = ehh[idx[funName]] + 24
				diff = ((ehh[idx[funName]] - shh[idx[funName]])*3600 + (emm[idx[funName]] - smm[idx[funName]])*60 + (ess[idx[funName]] - sss[idx[funName]]))*1000 + (ems[idx[funName]] - sms[idx[funName]])
				#print "diff:" + str(diff)
				timeT[idx[funName]].append(diff)
				#reset the start time and end time to 0
				shh[idx[funName]] == 0  
				smm[idx[funName]] == 0 
				sss[idx[funName]] == 0
				sms[idx[funName]] == 0
				ehh[idx[funName]] == 0  
				emm[idx[funName]] == 0 
				ess[idx[funName]] == 0
				ems[idx[funName]] == 0
	totalTime = [0.0] * 5
	for i in range(len(timeT)):
		for j in range(len(timeT[i])):
			totalTime[i] = totalTime[i] + timeT[i][j]
	avgTime = [0.0] * 5
	for i in range(len(totalTime)):
		if (len(timeT[i]) > 0):
			avgTime[i] = totalTime[i]/len(timeT[i])
			print str(totalTime[i]) + ":" + str(len(timeT[i])) + ":" + str(avgTime[i])
		else:
			avgTime[i] = -1.0

	resF.write("read packet time: " + str(avgTime[0]) + "ms\n")
	resF.write("decode time: " + str(avgTime[1]) + "ms\n")
	resF.write("scale and color conversion time: " + str(avgTime[2]) + "ms\n")
	resF.write("draw on canvas time: " + str(avgTime[3]) + "ms\n")
	resF.write("seek time: " + str(avgTime[4]) + "ms\n")

	resF.close()
	logF.close()
