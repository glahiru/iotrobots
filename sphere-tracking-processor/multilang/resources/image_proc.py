import storm

from lib import libardrone
from lib import h264decoder
import time
import sys
import threading
import base64
import json
from StringIO import StringIO

from modules.TrackingSensorModule import TrackingSensorModule

class SplitSentenceBolt(storm.BasicBolt):
    def __init__(self):
        self.trackingSensorModule = TrackingSensorModule()
#        self.decoder = h264decoder.H264Decoder((360, 640, 3))

    def process(self, tup):
        base64Frame = [elem.encode("hex") for elem in tup.values[0]]

        frame =  base64.b64decode(base64Frame)

        #circles = self.trackingSensorModule.detectCircles(frame)
        circles = []
        message = {}

#        if circles is not None:
#            if circles.size == 3:
#                position = self.trackingSensorModule.calculatePosition(circles)
#                message["control"] = {"position" : [position[0], position[1]]}
#            else:
#                message["control"] = {"hover" : "true"}
#        else:
        message["control"] = {"hover" : "true"}

        io = StringIO()
        json.dump(message, io)
        storm.emit([io.getvalue()])

SplitSentenceBolt().run()