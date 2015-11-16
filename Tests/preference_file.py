import sys
import xml.etree.cElementTree as ET


def create(tracking_key, tracking_interval, sms_config, autostart_tracking, ip_address):
    """

    :param tracking_key:
    :param tracking_interval:
    :param sms_config:
    :param autostart_tracking:
    :param ip_address:
    """
    map = ET.Element("map")
    ET.SubElement(map, "string", name="tracking_key").text = tracking_key
    ET.SubElement(map, "string", name="tracking_interval").text = tracking_interval
    ET.SubElement(map, "boolean", name="sms_config", value=sms_config)
    ET.SubElement(map, "boolean", name="autostart_tracking", value=autostart_tracking)
    ET.SubElement(map, "string", name="ip_address").text = ip_address
    tree = ET.ElementTree(map)
    tree.write("ch.luethi.skylinestracker_preferences.xml", encoding='utf-8', xml_declaration=True)


##create("XXX", "5", "false", "false", "192.168.1.43")
if __name__ == "__main__":
    print sys.argv[1]
    create(sys.argv[1],sys.argv[2],sys.argv[3],sys.argv[4],sys.argv[5])
