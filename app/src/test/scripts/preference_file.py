import sys
import xml.etree.cElementTree as ET


def create(tracking_key, tracking_interval, autostart_tracking, ip_address, queue_fixes, queue_fixes_max):
    """

    :param tracking_key:
    :param tracking_interval:
    :param autostart_tracking:
    :param ip_address:
    :param queue_fixes:
    :param queue_fixes_max:
    """
    map = ET.Element("map")
    ET.SubElement(map, "string", name="tracking_key").text = tracking_key
    ET.SubElement(map, "string", name="tracking_interval").text = tracking_interval
    ET.SubElement(map, "boolean", name="autostart_tracking", value=autostart_tracking)
    ET.SubElement(map, "string", name="ip_address").text = ip_address
    ET.SubElement(map, "boolean", name="queue_fixes", value=queue_fixes)
    ET.SubElement(map, "int", name="queue_fixes_max_seconds", value=queue_fixes_max)

    tree = ET.ElementTree(map)
    tree.write("ch.luethi.skylinestracker_preferences.xml", encoding='utf-8', xml_declaration=True)


if __name__ == "__main__":
    create(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4], sys.argv[5], sys.argv[6])
