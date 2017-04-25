import cv2
import numpy as np
import imutils
import sys
from skimage import exposure
from pytesseract import image_to_string
import PIL


def preproc(img_file):

    # first, the image is read in using OpenCVs imread function then resized to reduce computational load
    img_small = imutils.resize(img, height=300)

    # bilateral filter is an edge preserving noise reducing filter
    return cv2.cvtColor(img_small , cv2.COLOR_BGR2GRAY) # convert to gray scale

def filter_and_edge(imgray):
    # bilateral filter is an edge preserving noise reduction nonlinear filter
    # two pixels are considered close if they are not only close with spacial consideration, but also in intensity
    # arg[0] = image to be filtered
    # arg[1] = diameter of pixel neighborhood used
    # arg[2] = filter sigma of color space (larger number = larger area of semi-equal color)
    # arg[3] = filter sigma in coordinate space (larger value means farther pixels will have more impact
    #           if they are of similar color)
    bilat_img = cv2.bilateralFilter(imgray, 11, 17, 17)

    # Canny edge detection is a computationally expensive, but an optimal detector
    # this means:
    #       1. Low error rate: Meaning a good detection of only existent edges.
    #       2. Good localization: The distance between edge pixels detected and real edge pixels have to be minimized.
    #       3. Minimal response: Only one detector response per edge.
    # if the pixel gradient is above the upper threshold then the edge is accepted
    # if the pixel gradient is below the lower threshold then it is rejected
    # finally, if the pixel gradient is between the two it is only accepted if it connects to an accepted pixel
    return cv2.Canny(bilat_img, 70, 200)

def contour_and_extract_norm_display(img, edged_img):
    # finds the contours of objects in the image, it is used here to find the display
    # sorting the contours by area returns a largest first list of contour areas
    # taking the top ten gives us a good chance of finding the rectangular display
    im2, contours, hierarchy = cv2.findContours(edged_img, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
    top_cntrs = sorted(contours, key=cv2.contourArea, reverse=True)[:10]

    # examine the top 10 contours and look for the rectangular display contour
    display_contour = None
    for cntr in top_cntrs:
        epsilon = 0.1 * cv2.arcLength(cntr, True)
        approx = cv2.approxPolyDP(cntr, epsilon, True) # approximates polygonal curves with a specified precision

        # look for a polygon that is composed of 4 points, this is our display (theoretically anyway)
        if len(approx) == 4:
            display_contour = approx
            break

    # reshape the display contour to be a list of four tuples
    cntr_points = display_contour.reshape(4,2)
    ratio = img.shape[0] / 300.0 # get the ratio of the original to the reduced image
    norm_pts = np.zeros((4,2), dtype="float32")

    # clever way to find the correct ordering of the contour points
    # this finds the top left and bottom right using the min and max sums
    s = cntr_points.sum(axis=1)
    norm_pts[0] = cntr_points[np.argmin(s)]
    norm_pts[2] = cntr_points[np.argmax(s)]

    # and this finds the top right and bottom left using the min and max differences
    d = np.diff(cntr_points, axis=1)
    norm_pts[1] = cntr_points[np.argmin(d)]
    norm_pts[3] = cntr_points[np.argmax(d)]

    # now normalize to fit the original image
    return norm_pts * ratio

def warp_perspective(img, norm_pts):
    # find the lengths of each and then take the longest to be the width and height
    (top_left, top_right, bottom_right, bottom_left) = norm_pts

    width1 = np.sqrt(((bottom_right[0] - bottom_left[0]) ** 2) + ((bottom_left[1] - bottom_right[1]) ** 2))
    width2 = np.sqrt(((top_right[0] - top_left[0]) ** 2) + ((top_right[1] - top_left[1]) ** 2))
    height1 = np.sqrt(((bottom_right[0] - top_right[0]) ** 2) + ((bottom_right[1]- top_right[1]) ** 2))
    height2 = np.sqrt(((bottom_left[0] - top_left[0]) ** 2) + ((bottom_left[1] - top_left[1]) ** 2))

    max_width = max(int(width1), int(width2))
    max_height = max(int(height1), int(height2))

    # creates a new array to hold only the display that was just found
    # find the perspective transform between the normalized points (corresponding to the original image)
    # finally warp the perspective so that only the display remains
    dst = np.array([[0,0], [max_width - 1, 0], [max_width - 1, max_height - 1],[0, max_height - 1]], dtype="float32" )
    persp_matrix = cv2.getPerspectiveTransform(norm_pts,dst)
    return cv2.warpPerspective(img, persp_matrix, (max_width, max_height))


def proc_with_tesseract(display_image):
    # convert warped image to gray scale, then rescale to be black and white
    gry_display = cv2.cvtColor(display_image, cv2.COLOR_BGR2GRAY)
    gry_display = exposure.rescale_intensity(gry_display, out_range= (0,255))

    # if the intensity of a pixel is over 70 then it is converted to 255 (white)
    ret, thresh = cv2.threshold(gry_display, 50, 255, cv2.THRESH_BINARY)
    cv2.imwrite('thresh.jpg', thresh) # for testing purposes to see what file will be read by tesseract

    # create a new PIL image to output the image to tesseract
    thresh = PIL.Image.fromarray(thresh)
    return image_to_string(thresh, lang="eng", config="-psm 100 -c tessedit_char_whitelist=.0123456789")

if __name__ == "__main__":
    args = sys.argv
    if len(args) > 1:
        print "Processing image..."
        img = cv2.imread(args[1])
        small_gray_img = preproc(img)
        filtered_edged_img = filter_and_edge(small_gray_img)
        display_contours = contour_and_extract_norm_display(img, filtered_edged_img)
        warped_display = warp_perspective(img, display_contours)
        ocr_value = proc_with_tesseract(warped_display)
        print "tesseract read: " + ocr_value
        print "...Done"
