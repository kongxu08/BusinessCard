package checkauto.camera.com.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;

import com.kernal.demo.bucard.R;

import kernel.BusinessCard.android.Frame;


public final class ViewfinderView extends View {
    private int checkLeftFrame = 0;// 检测证件左边是否存在或者对齐
    private int checkTopFrame = 0;// 检测证件上边是否存在或者对齐
    private int checkRightFrame = 0;// 检测证件右边是否存在或者对齐
    private int checkBottomFrame = 0;// 检测证件下边是否存在或者对齐
    private static final int[] SCANNER_ALPHA = { 0, 64, 128, 192, 255, 192,
            128, 64 };
    /**
     * 刷新界面的时间
     */
    private static final long ANIMATION_DELAY = 20L;

    public void setCheckLeftFrame(int checkLeftFrame) {
        this.checkLeftFrame = checkLeftFrame;
    }
    public void setCheckTopFrame(int checkTopFrame) {
        this.checkTopFrame = checkTopFrame;
    }

    public void setCheckRightFrame(int checkRightFrame) {
        this.checkRightFrame = checkRightFrame;
    }

    public void setCheckBottomFrame(int checkBottomFrame) {
        this.checkBottomFrame = checkBottomFrame;
    }

    private static final int OPAQUE = 0xFF;
    private final Paint paint;
    private int scannerAlpha;
    /**
     * 中间滑动线的最顶端位置
     */
    private int slideTop;
    private int slideTop1;

    /**
     * 中间滑动线的最底端位置
     */
    private int slideBottom;
    /**
     * 中间那条线每次刷新移动的距离
     */
    private static final int SPEEN_DISTANCE = 10;
    /**
     * 扫描框中的线的宽度
     */
    private static final int MIDDLE_LINE_WIDTH = 6;
    private boolean isFirst = false;
    /**
     * 四周边框的宽度
     */
    private static final int FRAME_LINE_WIDTH = 4;
    private Rect frame;
    private Frame frame1;
    private int width;
    private int height;
    private int nCropType;
    private   Resources resources;
    public void setnCropType(int nCropType) {
        this.nCropType = nCropType;
    }
    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        paint = new Paint();
        resources = getResources();
        scannerAlpha = 0;
    }
      public void setFocues(int nCropType, Frame frame){
          this.nCropType=nCropType;
          this.frame1=frame;
          postInvalidateDelayed(ANIMATION_DELAY, 0, 0, width, height);
 }
    @Override
    public void onDraw(Canvas canvas) {
        width = canvas.getWidth();
        height = canvas.getHeight();
      //  Log.i("string","画布的宽度为:"+width+"画布的高度为:"+height);

        // 初始化中间线滑动的最上边和最下边
        if (!isFirst) {
            isFirst = true;
            slideTop = width / 3;
            slideBottom = 2 * width / 3;
            slideTop1 = height / 3;
        }

if(nCropType==0){
    // 画出扫描框外面的阴影部分，共四个部分，扫描框的上面到屏幕上面，扫描框的下面到屏幕下面
    // 扫描框的左边面到屏幕左边，扫描框的右边到屏幕右边
    drawText(paint,canvas,height,width);
    frame = new Rect((int) (width * 0.15),
            (int) (height - 0.41004673 * width) / 2, (int) (width * 0.8),
            (int) (height + 0.41004673 * width) / 2);
    if (frame == null) {
        return;
    }
    paint.setColor(Color.argb(80, 100, 100, 100));
    canvas.drawRect(0, 0, width, frame.top, paint);
    canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
    canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1,
            paint);
    canvas.drawRect(0, frame.bottom + 1, width, height, paint);

    // 绘制两个像素边宽的绿色线框
    paint.setColor(Color.rgb(77,223, 68));
    canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2, frame.top,
            frame.left + FRAME_LINE_WIDTH - 2 + 50, frame.top
                    + FRAME_LINE_WIDTH, paint);
    canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2, frame.top,
            frame.left + FRAME_LINE_WIDTH + 2, frame.top + 50, paint);// 左上角
    canvas.drawRect(frame.right - FRAME_LINE_WIDTH - 2, frame.top,
            frame.right - FRAME_LINE_WIDTH + 2, frame.top + 50, paint);
    canvas.drawRect(frame.right - FRAME_LINE_WIDTH - 2 - 50, frame.top,
            frame.right - FRAME_LINE_WIDTH + 2, frame.top
                    + FRAME_LINE_WIDTH, paint);// 右上角
    canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2, frame.bottom - 50,
            frame.left + FRAME_LINE_WIDTH + 2, frame.bottom, paint);
    canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2, frame.bottom
                    - FRAME_LINE_WIDTH, frame.left + FRAME_LINE_WIDTH - 2 + 50,
            frame.bottom, paint); // 左下角
    canvas.drawRect(frame.right - FRAME_LINE_WIDTH - 2, frame.bottom - 50,
            frame.right - FRAME_LINE_WIDTH + 2, frame.bottom, paint);
    canvas.drawRect(frame.right - FRAME_LINE_WIDTH - 2 - 50, frame.bottom
                    - FRAME_LINE_WIDTH, frame.right - FRAME_LINE_WIDTH - 2,
            frame.bottom, paint); // 右下角
    // 如果检测到证件左边就会画出左边的提示线
    if (checkLeftFrame == 1)
        canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2, frame.top,
                frame.left + FRAME_LINE_WIDTH + 2, frame.bottom, paint);// 左边
    // 如果检测到证件上边就会画出左边的提示线
    if (checkTopFrame == 1)
        canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2, frame.top,
                frame.right - FRAME_LINE_WIDTH + 2, frame.top
                        + FRAME_LINE_WIDTH, paint);// 上边
    // 如果检测到证件右边就会画出左边的提示线
    if (checkRightFrame == 1)
        canvas.drawRect(frame.right - FRAME_LINE_WIDTH - 2, frame.top,
                frame.right - FRAME_LINE_WIDTH + 2, frame.bottom, paint);// 右边
    // 如果检测到证件底边就会画出左边的提示线
    if (checkBottomFrame == 1)
        canvas.drawRect(frame.left + FRAME_LINE_WIDTH - 2, frame.bottom
                        - FRAME_LINE_WIDTH, frame.right - FRAME_LINE_WIDTH - 2,
                frame.bottom, paint); // 右下角
}else if(nCropType==1&&frame1!=null&&!frame1.isNullFrame()){

    Path  path = new Path();
    paint.setStyle(Paint.Style.STROKE);//空心矩形框
    paint.setStrokeWidth((float)5.0);
    paint.setColor(Color.rgb(77,223, 68));
    paint.setAntiAlias(true);
    path.moveTo(frame1.top_x,frame1.top_y);
    path.lineTo(frame1.right_x,frame1.right_y);
    path.lineTo(frame1.bottom_x,frame1.bottom_y);
    path.lineTo(frame1.left_x,frame1.left_y);
    path.close();
    canvas.drawPath(path,paint);


    Path  top_path = new Path();
    paint.setStyle(Paint.Style.FILL);
    paint.setColor(Color.argb(80, 100, 100, 100));
    top_path.moveTo(0,0);
    top_path.lineTo(width,0);
    top_path.lineTo(frame1.right_x,frame1.right_y);
    top_path.lineTo(frame1.top_x,frame1.top_y);
    top_path.close();
    canvas.drawPath(top_path,paint);

    Path  left_path = new Path();
    left_path.moveTo(0,0);
    left_path.lineTo(frame1.top_x,frame1.top_y);
    left_path.lineTo(frame1.left_x,frame1.left_y);
    left_path.lineTo(0,height);
    left_path.close();
    canvas.drawPath(left_path,paint);

    Path  right_path = new Path();
    right_path.moveTo(frame1.right_x,frame1.right_y);
    right_path.lineTo(width,0);
    right_path.lineTo(width,height);
    right_path.lineTo(frame1.bottom_x,frame1.bottom_y);
    right_path.close();
    canvas.drawPath(right_path,paint);

    Path  bottom_path = new Path();
    bottom_path.moveTo(frame1.left_x,frame1.left_y);
    bottom_path.lineTo(frame1.bottom_x,frame1.bottom_y);
    bottom_path.lineTo(width,height);
    bottom_path.lineTo(0,height);
    path.close();
    canvas.drawPath(bottom_path,paint);
    paint.reset();

}else if(nCropType==1&&(frame1==null||frame1.isNullFrame())){
    drawText(paint,canvas,height,width);
    paint.setColor(Color.rgb(0, 255, 0));
    paint.setStrokeWidth(5);
    paint.setAntiAlias(true);
    paint.setStyle(Paint.Style.STROKE);
    PathEffect effects = new DashPathEffect(new float[]{10,10,10,10},1);
    paint.setPathEffect(effects);
    Path path1 = new Path();
    path1.moveTo((int) (width * 0.15), (int) (height - 0.41004673 * width) / 2);
    path1.lineTo((int) (width * 0.8), (int) (height - 0.41004673 * width) / 2);
    path1.lineTo((int) (width * 0.8), (int) (height + 0.41004673 * width) / 2);
    path1.lineTo((int) (width * 0.15), (int) (height + 0.41004673 * width) / 2);
    path1.close();// 封闭
    canvas.drawPath(path1, paint);
    paint.reset();
}
        /**
         * 当我们获得结果的时候，我们更新整个屏幕的内容
         */

        // postInvalidateDelayed(ANIMATION_DELAY, 0, 0, width, height);

    }

    private  void drawText(Paint paint,Canvas canvas,int height,int width){
          if(width<=1080){
              paint.setTextSize(25);
          }else{
              paint.setTextSize(40);//设置文字字体大小
          }

        paint.setTextAlign(Paint.Align.CENTER);
        // 计算Baseline绘制的Y坐标 ，计算方式：画布高度的一半 - 文字总高度的一半
        int baseY = (int) (height/2-((paint.descent() + paint.ascent()) / 2));
        paint.setColor(Color.WHITE);
        canvas.drawText(resources.getString(R.string.preview_hint_msg),width/2, baseY, paint);
    }
}
