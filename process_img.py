import cv2
import numpy as np
import imutils
import sys
from skimage import exposure
from pytesseract import image_to_string
import PIL

def main(img_file):
    img = cv2.imread(img_file)
    img = imutils.resize(img, height=300)
    cv2.imwrite("blah.jpg", img)
    bilat_img = cv2.bilateralFilter(img, 11, 17, 17)
    edged_img = cv2.Canny(bilat_img, 30, 200)
    imgray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    imgray_copy = imgray.copy()
    im2, contours, hierarchy = cv2.findContours(imgray_copy, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
    top_cntrs = sorted(contours, key=cv2.contourArea, reverse=True)[:10]

    display_contour = None
    for cntr in top_cntrs:
        epsilon = 0.02 * cv2.arcLength(cntr, True)
        approx = cv2.approxPolyDP(cntr, epsilon, True)

        print approx
        print len(approx)
        if len(approx) == 4:
            display_contour = approx
            break


    cntr_points = display_contour.reshape(4,2)
    print cntr_points
    ratio = img.shape[0] / 300.0
    norm_pts = np.zeros((4,2), dtype="float32")

    s = cntr_points.sum(axis=1)
    norm_pts[0] = cntr_points[np.argmin(s)]
    norm_pts[2] = cntr_points[np.argmax(s)]

    d = np.diff(cntr_points, axis=1)
    norm_pts[1] = cntr_points[np.argmin(d)]
    norm_pts[3] = cntr_points[np.argmax(d)]

    norm_pts *= ratio

    (top_left, top_right, bottom_left, bottom_right) = norm_pts

    width1 = np.sqrt(((bottom_right[0] - bottom_left[0]) ** 2) + ((bottom_left[1] - bottom_right[1]) ** 2))
    width2 = np.sqrt(((top_right[0] - top_left[0]) ** 2) + ((top_right[1] - top_left[1]) ** 2))
    height1 = np.sqrt(((bottom_right[0] - top_right[0]) ** 2) + ((bottom_right[1]- top_right[1]) ** 2))
    height2 = np.sqrt(((bottom_left[0] - top_left[0]) ** 2) + ((bottom_left[1] - top_left[1]) ** 2))

    max_width = max(int(width1), int(width2))
    max_height = max(int(height1), int(height2))

    dst = np.array([[0,0], [max_width - 1, 0], [max_width - 1, max_height - 1],[0, max_height - 1]], dtype="float32" )
    persp_matrix = cv2.getPerspectiveTransform(norm_pts,dst)

    display_image = cv2.warpPerspective(img, persp_matrix, (max_width, max_height))

    gry_display = cv2.cvtColor(display_image, cv2.COLOR_BGR2GRAY)
    gry_display = exposure.rescale_intensity(gry_display, out_range= (0,255))

    ret, thresh = cv2.threshold(gry_display, 70, 255, cv2.THRESH_TOZERO)
    cv2.imwrite('thresh.jpg', thresh)
    thresh = PIL.Image.fromarray(thresh)
    print image_to_string(thresh, lang="eng", config="-psm 100 -c tessedit_char_whitelist=.0123456789")

if __name__ == "__main__":
    args = sys.argv
    if len(args) > 1:
        print "Processing image..."
        main(args[1])
        print "...Done"
