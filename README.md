# 对新闻客户端APP的性能优化



## 1.  解决启动白屏问题

> 当系统加载并启动 App 时，需要耗费相应的时间，这样会造成用户会感觉到当点击 App 图标时会有 “延迟” 现象， 为了解决这一问题，Google 的做法是在 App 创建的过程中，先展示一个空白页面，让用户体会到点击图标之后立 马就有响应。 如果你application或activity启动的过程太慢，导致系统的BackgroundWindow没有及时被替换，就会出现启动 时白屏或黑屏的情况（取决于Theme主题是Dark还是Light）。 消除启动时的黑/白屏问题，大部分App都采用自己在Theme中设置背景图的方式来解决。

在Style里面配置

```xml
<style name = "AppTheme.Luncher">
    <item name="android:windowBackground">@drwaable/windows_bg</item>
    <item name="android:windowFullscreen">true</item>
</style>
```

```xml
windows_bg
<layer-list xmlns:andrid=............>
    <item android:drawable="@android:color/white"/>
    <item>
        <bitmap
                android:gravity="center"
                android:scaleType="centercrop"
                android:src="@drawable/bg" />
    </item>
</layer-list>
```

在manifest.xml 中

```xml
........................
	<application
                ..............
                 ............
                 >
        <activity
                  android:name=".Mainactivity"
                  android:theme="@style/Apptheme.luncher">
```

在Activity的onCreate 改回原来的

```java
public class onCreate() {
	setTheme(R.style.AppTheme);
}
```

解决了白屏问题， 启动APK 会加载一个新闻的图标



## 2. 启动耗时优化

> 在性能测试中存在启动时间2-5-8原则： 
>
> - 当用户能够在2秒以内得到响应时，会感觉系统的响应很快； 
> - 当用户在2-5秒之间得到响应时，会感觉系统的响应速度还可以；
> - 当用户在5-8秒以内得到响应时，会感觉系统的响应速度很慢，但是还可以接受； 
> - 而当用户在超过8秒后仍然无法得到响应时，会感觉系统糟透了，或者认为系统已经失去响应。

​	 而Google 也提出一项计划：Android Vitals 。该计划旨在改善 Android 设备的稳定性和性能。当选择启用了该计 划的用户运行您的用时，其 Android 设备会记录各种指标，包括应用稳定性、应用启动时间、电池使用情况、呈 现时间和权限遭拒等方面的数据。Google Play 管理中心 会汇总这些数据，并将其显示在 Android Vitals 信息中心 内。 当应用启动时间过长时，Android Vitals 可以通过 Play 管理中心提醒您，从而帮助提升应用性能。

Android Vitals 在您的应用出现以下情况时将其启动时间视为过长： 

- 冷启动用了 5 秒或更长时间。 
- 温启动用了 2 秒或更长时间。 
- 热启动用了 1.5 秒或更长时间。

 实际上不同的应用因为启动时需要初始化的数据不同，启动时间自然也会不同。相同的应用也会因为在不同的设 备，因为设备性能影响启动速度不同。所以实际上启动时间并没有绝对统一的标准，我们之所以需要进行启动耗时 的统计的，可能在于产品对我们应用启动时间提出具体的要求。

### 2.1 优化前

来看看优化之前启动耗时的最小值

```shell
adb shell am start -S -W packageName/LunchActivity

ThisTime : 3340 ms
TotalTime : 3340 ms
waitTime : 3413 ms
```

- ​	WaitTime:总的耗时，包括前一个应用Activity pause的时间和新应用启动的时间； 
- ​	ThisTime表示一连串启动Activity的最后一个Activity的启动耗时； 
- ​	TotalTime表示新应用启动的耗时，包括新进程的启动和Activity的启动，但不包括前一个应用Activity pause 的耗时。

开发者一般只要关心TotalTime即可，这个时间才是自己应用真正启动的耗时。启动时间总花费了 3340 ms， 大概3 秒的样子，当用户在2-5秒之间得到响应，只能说感觉系统的响应速度还可以， 但能不能更快呢？能够在2秒以内得到响应时，会感觉系统的响应很快。

### 2.2 启动优化工具 CPU Profile/TraceView

​	如果发现显示时间比希望的时间长，则可以继续尝试识别启动过程中的瓶颈。 查找瓶颈的一个好方法是使用 Android Studio CPU 性能剖析器。402 Traceview是android平台配备一个很好的性能分析的工具。它可以通过图形化的方式让我们了解我们要跟踪 的程序的性能，并且能具体到每个方法的执行时间。但是目前Traceview 已弃用。如果使用 Android Studio 3.2 或更高版本，则应改为使用 CPU Profiler 要在应用启动过程中自动开始记录 CPU 活动，请执行以下操作：

要在应用启动过程中自动开始记录 CPU 活动，请执行以下操作：

1. 依次选择 Run > Edit Configurations。
2. 在 Profiling 标签中，勾选 Start recording CPU activity on startup 旁边的复选框
3. 从菜单中选择 CPU 记录配置。

   1. Sample Java Methods 对 Java 方法采样：在应用的 Java 代码执行期间，频繁捕获应用的调用堆栈。分析器会比较捕获的数据集， 以推导与应用的 Java 代码执行有关的时间和资源使用信息。如果应用在捕获调用堆栈后进入一个方法并在下 次捕获前退出该方法，分析器将不会记录该方法调用。如果您想要跟踪生命周期如此短的方法，应使用检测 跟踪。 

   2. Trace Java Methods 跟踪 Java 方法：在运行时检测应用，以在每个方法调用开始和结束时记录一个时间戳。系统会收集并比较这 些时间戳，以生成方法跟踪数据，包括时间信息和 CPU 使用率。 

   3. Sample C/C++ Functions 对 C/C++ 函数采样：捕获应用的原生线程的采样跟踪数据。要使用此配置，您必须将应用部署到搭载 Android 8.0（API 级别 26）或更高版本的设备上。 

   4. Trace System Calls 跟踪系统调用：捕获非常翔实的细节，以便您检查应用与系统资源的交互情况。您可以检查线程状态的确切 时间和持续时间、直观地查看所有内核的 CPU 瓶颈在何处，并添加要分析的自定义跟踪事件。要使用此配 置，您必须将应用部署到搭载 Android 7.0（API 级别 24）或更高版本的设备上。 此跟踪配置在 systrace 的基础上构建而成。您可以使用 systrace 命令行实用程序指定除 CPU Profiler 提供的 选项之外的其他选项。systrace 提供的其他系统级数据可帮助您检查原生系统进程并排查丢帧或帧延迟问 题。
4. 点击 Apply。
5. 依次选择 Run > Profile，将您的应用部署到搭载 Android 8.0（API 级别 26）或更高版本的设备上

本次调查不通过cpu profile 的方式 trace java method 进行采样， 太卡, 而是通过debug API的方式进行采样处理

```java
public class ArchDemoApplication extends Application {
    Debug.startMethodTracingSample(new File(Environment.getExternalStarageDirectory(), "enjoy").getAbsolutePath(),8*1024*1024,1_1000);
}
```

在 mainActivity 的 onWindowFocusChange(boolean hasFocus)  手指点app 可以响应，需要申请文件读取权限。

```java
public class MyApplication extends Application {
	public MyApplication() {
		Debug.startMethodTracing("enjoy");
		}
		//.....
	}
public class MainActivity extends AppCompatActivity {
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		Debug.stopMethodTracing();
	}
	//.......
}

```

运行App，则会在sdcard中生成一个enjoy.trace文件（需要sdcard读写权限）。将手机中的trace文件保存至电 脑，随后拖入Android Studio即可。把生成的enjoy文件拖入到AS中

![image-20220619202854534](https://user-images.githubusercontent.com/30100887/174488991-627c6d9a-4d26-442b-b9d2-ffb8095b85ea.png)

**Call Chart** 

​	以图形来呈现方法跟踪数据或函数跟踪数据，其中调用的时间段和时间在横轴上表示，而其被调用方则在纵轴上显 示。对系统 API 的调用显示为橙色，对应用自有方法的调用显示为绿色，对第三方 API（包括 Java 语言 API）的调 用显示为蓝色。 （实际颜色显示有Bug）

![image-20220619204623416](https://user-images.githubusercontent.com/30100887/174488872-40305199-5b60-4342-a932-263527ae0942.png)

如上图，自定义Application的 onCreate 调用了 Thread.sleep 耗时为：3s。 

Call Chart 已经比原数据可读性高很多，但它仍然不方便发现那些运行时间很长的代码，这时我们便需要使用 Flame Chart。

**Flame Chart**

提供一个倒置的调用图表，用来汇总完全相同的调用堆栈。也就是说，将具有相同调用方顺序的完全相同的方法或 函数收集起来，并在火焰图中将它们表示为一个较长的横条 。

横轴显示的是百分比数值。由于忽略了时间线信息，Flame Chart 可以展示每次调用消耗时间占用整个记录时长的 百分比。 同时纵轴也被对调了，在顶部展示的是被调用者，底部展示的是调用者。此时的图表看起来越往上越窄， 就好像火焰一样，因此得名: 火焰图。


**Top Down Tree**

如果我们需要更精确的时间信息，就需要使用 Top Down Tree。 Top Down Tree显示一个调用列表，在该列表中 展开方法或函数节点会显示它调用了的方法节点。

对于每个节点，三个时间信息: 

- Self Time —— 运行自己的代码所消耗的时间； 
- Children Time —— 调用其他方法的时间；
- Total Time —— 前面两者时间之和。

**Bottom Up Tree**

方便地找到某个方法的调用栈。在该列表中展开方法或函数节点会显示哪个方法调用了自己。

通过工具可以定位到耗时代码，然后查看是否可以进行优化。对于APP启动来说，启动耗时包括Android系统启动 APP进程加上APP启动界面的耗时时长，我们可做的优化是APP启动界面的耗时，也就是说从Application的构建到 主界面的 onWindowFocusChanged 的这一段时间。 因此在这段时间内，我们的代码需要尽量避免耗时操作，检查的方向包括：主线程IO；第三方库初始化或程序需要 使用的数据等初始化改为异步加载/懒加载；减少布局复杂度与嵌套层级；Multidex(5.0以上无需考虑)等。


### 2.3 优化点 1 优化 SharedPreference

![image-20220619155148739](https://user-images.githubusercontent.com/30100887/174489003-97511fc4-e99c-4741-8260-a66f3bdba038.png)

SharedPreference 涉及到 在子线程进行XML读取， SP 的定位就是保存简单轻量级的 key value 数据， 虽然在子线程读取， 但如果主线程需要读取结果， 那就也需要对 sp 进行优化。

### 2.4 布局异步加载

LayoutInflater加载xml布局的过程会在主线程使用IO读取XML布局文件进行XML解析，再根据解析结果利用反射 创建布局中的View/ViewGroup对象。这个过程随着布局的复杂度上升，耗时自然也会随之增大。Android为我们 提供了 Asynclayoutinflater 把耗时的加载操作在异步线程中完成，最后把加载结果再回调给主线程。

```javascript
dependencies {
implementation "androidx.asynclayoutinflater:asynclayoutinflater:1.0.0"
}
```

```java
new AsyncLayoutInflater(this)
.inflate(R.layout.activity_main, null, new AsyncLayoutInflater.OnInflateFinishedListener() {
	@Override
	public void onInflateFinished(@NonNull View view, int resid, @Nullable ViewGroup parent) {
		setContentView(view);
		//......
	}
});

```

1、使用异步 inflate，那么需要这个 layout 的 parent 的 generateLayoutParams 函数是线程安全的； 

2、所有构建的 View 中必须不能创建 Handler 或者是调用 Looper.myLooper；（因为是在异步线程中加载的，异 步线程默认没有调用 Looper.prepare ）； 

3、AsyncLayoutInflater 不支持设置 LayoutInflater.Factory 或者 LayoutInflater.Factory2；

4、不支持加载包含 Fragment 的 layout 

5、如果 AsyncLayoutInflater 失败，那么会自动回退到UI线程来加载布局；

### 2.5 优化点2  新闻数据缓存 异步加载 

在fragment的onCreate的时候读取了缓存的数据和 SP， 占用了一定的时间，改为了通过IdleHandler 进行空闲时间异步加载

```java
public class headLineNewsViewModel extend MvvmBaseViewModel<ChannelsModel, ChannelsModel.Channel> {
	public HeadLineNowsViewModel() {
		model = new ChannelsModel();
		model.register(this);
		
		looper.myQueue().addIdleHandler(new MessageQueue.IdeleHandler() {
			@Override
			public boolean queueIdle() {
				model.getCacheDataAndload();
				return false;
			}
		});
	}
}
```

### 2.6 代码优化

在 DispatchMessage的 onChange 中耗费了 700 多的毫秒数，  有两处调用了notifyDataSetChange, 如图



![image-20220619153108667](https://user-images.githubusercontent.com/30100887/174489124-0c531aac-3ac6-46ca-9332-15ca47c255ff.png)

删除其中一个可以优化 快300 ms

再启动 一次 看看启动时间, 优化了大约 500 ms

```shell
ThisTime 2801
TotalTime 2801
WaitTime 3457
```



### 3.6 布局优化

> ​		measure、layout、draw这三个过程都包含自顶向下的View Tree遍历耗时，如果视图层级太深自然需要更多的时 间来完成整个绘测 
>
> 过程，从而造成启动速度慢、卡顿等问题。而onDraw在频繁刷新时可能多次出发，因此 onDraw更不能做耗时操作，同时需要注意内存 
>
> 抖动。对于布局性能的检测，依然可以使用systrace与traceview按 照绘制流程检查绘制耗时函数。

![image-20220619230236944](https://user-images.githubusercontent.com/30100887/174489140-ee27a719-f7b3-4384-863b-fdeaed35a42d.png)

在左侧id为content之下的就是我们写在XML中的布局。可以明显看出，我们的布局中是一个 LinearLayout ,其中 又包含两个 LinearLayout 。我们应该尽量减少其层级，可以使用ConstraintLayout 约束布局使得布局尽量扁平 化，移除非必需的UI组件。

在SetContentView 加载布局的时候可不可进行优化呢，加快加载时间。

![image-20220619153736460](https://user-images.githubusercontent.com/30100887/174489152-62704889-4a28-44b6-b049-ebe2c47a9b4a.png)



Tools -> LayoutInspector

![image-20220619163037744](https://user-images.githubusercontent.com/30100887/174489170-afa482c9-36f4-47a3-b524-c7b28696d173.png)

在ManiActiviy的布局中，上下两个都是调用的系统布局，主要看看我们自定义的 Framelayout 里面有没有可以优化的点

Framelayout主要是被 headlineNewsFragment 索替换， 所以重点看依稀 headlineNewsFragment的布局可不可以被优化



#### 3.6.1 优化点1  viewPage懒加载

viewPager 替换为  ViewPage2

```xml
<androidx.viewpager2.widget.ViewPager2 />
```

**ViewPager2默认就实现了懒加载**。 但是如果想避免Fragment频繁销毁和创建造成的开销，可以通过 setOffscreenPageLimit () 方法设置预加载数量，将数据加载逻辑放到Fragment的 onResume () 方法中。

```java
viewDataBinding.viewpager2.setOffscreenPageLimit(1)
```

#### 3.6.2 优化点2 优化 TitleView

![image-20220619164454724](https://user-images.githubusercontent.com/30100887/174489197-bd8ebbc4-8fee-4223-9c00-a0512cd0f2b9.png)

TItleView里面嵌套了一层LinearLayout, 为什么呢？ 进去看一下， 能不能优化掉这层LinearLayout

![image-20220619164709373](https://user-images.githubusercontent.com/30100887/174489203-0d4e5271-cd9d-49ae-915d-2a377999cd44.png)

​	因为 TitleView 需要在 RecycleView 里面加了一个分割线，为了方便，直接在xml中添加了分割线 ， 那我们可以去掉这个分割线，仅留下一个TextView，不就可以去掉外面一层的LinearLayout 了吗， 把分割线在代码中实现：

```java
public voic onCreate() {
    ..............
    viewDataBinding.listView.addItemDecoration(new RecycleViewDivider(gerContext(), LinearLayoutManager.HORIZONNTAL))l
}
```

#### 3.6.3 PictureTitleLayout 优化

![image-20220619170855337](https://user-images.githubusercontent.com/30100887/174489216-4007198e-b2ac-406a-9d40-3fc980068d41.png)

使用Merge标签

当我们有一些布局元素需要被多处使用时，这时候我们会考虑将其抽取成一个单独的布局文件。在需要使用的地方 通过 include 加载。

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
	android:layout_width="match_parent"
	android:layout_height="match_parent"
	android:background="#000000"
	android:orientation="vertical">
	<!-- include layout_merge布局 -->
	<include layout="@layout/layout_merge" />
</LinearLayout>

<!-- layout_merge -->
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
	android:layout_width="wrap_content"
	android:layout_height="wrap_content"
	android:orientation="vertical">
    
	<TextView
		android:background="#ffffff"
		android:layout_width="wrap_content"
		android:layout_height="wrap_content"
		android:text="测试merge" />
</LinearLayout>
```

这时候我们的主布局文件是垂直的LinearLayout，include的 "layout_merge" 也是垂直的LinearLayout，这时候 include的布局中使用的LinearLayout就没意义了，使用的话反而减慢你的UI表现。这时可以使用merge标签优 化。

```xml
<!-- layout_merge -->
<merge xmlns:android="http://schemas.android.com/apk/res/android">
	<TextView
		android:background="#ffffff"
		android:layout_width="wrap_content"
		android:layout_height="wrap_content"
		android:text="测试merge" />
</merge>
```

修改为merge后，通过LayoutInspector能够发现，include的布局中TextView直接被加入到父布局中。

![image-20220619231515654](https://user-images.githubusercontent.com/30100887/174489230-0faab96c-866f-4194-a52f-1a5f08935d64.png)

**使用ViewStub 标签**

当我们布局中存在一个View/ViewGroup，在某个特定时刻才需要他的展示时，可能会有同学把这个元素在xml中 定义为invisible或者gone，在需要显示时再设置为visible可见。比如在登陆时，如果密码错误在密码输入框上显示 提示。

- invisible view设置为invisible时，view在layout布局文件中会占用位置，但是view为不可见，该view还是会创建对 象，会被初始化，会占用资源。 
- gone view设置gone时，view在layout布局文件中不占用位置，但是该view还是会创建对象，会被初始化，会占 用资源。

如果view不一定会显示，此时可以使用 ViewStub 来包裹此View 以避免不需要显示view但是又需要加载view消耗资 源。 viewstub是一个轻量级的view，它不可见，不用占用资源，只有设置viewstub为visible或者调用其inflater()方法 时，其对应的布局文件才会被初始化。

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
	android:layout_width="match_parent"
	android:layout_height="match_parent"
	android:background="#000000"
	android:orientation="vertical">
	<ViewStub
		android:id="@+id/viewStub"
		android:layout_width="600dp"
		android:layout_height="500dp"
		android:inflatedId="@+id/textView"
		android:layout="@layout/layout_viewstub" />
</LinearLayout>
	<!-- layout_viewstub -->
	<?xml version="1.0" encoding="utf-8"?>
	<TextView xmlns:android="http://schemas.android.com/apk/res/android"
		android:layout_width="wrap_content"
		android:layout_height="wrap_content"
		android:background="#ffffff"
		android:text="测试viewStub" />
```

加载viewStub后，可以通过 inflatedId 找到layout_viewstub 中的根View。

再看一下现在的耗时，又优化了 600 ms

```shell
ThisTime 2202
TotalTime 2320
waitTime 3045
```

总体的 启动优化大概优化了快 1 S 的样子， 虽然没有优化到2 S 以内， 但也对启动的性能有了很大的提高。



## 3. 卡顿优化

> 大多数用户感知到的卡顿等性能问题的最主要根源都是因为渲染性能。Android系统每隔大概16.6ms发出VSYNC信 号，触发对UI进行渲染，如果每次渲染都成功，这样就能够达到流畅的画面所需要的60fps，为了能够实现60fps， 这意味着程序的大多数操作都必须在16ms内完成。开发app的性能目标就是保持60fps，这意味着每一帧你只有16ms=1000/60的时间来处理所有的任务。

![image-20220619212237823](https://user-images.githubusercontent.com/30100887/174489244-27a7b3a9-3fe4-4d7a-b800-d1f3239f5e4b.png)

如果某个操作花费时间是24ms，系统在得到VSYNC信号的时候就无法进行正常渲染，这样就发生了丢帧现象。那 么用户在32ms内看到的会是同一帧画面。

![image-20220619212325906](https://user-images.githubusercontent.com/30100887/174489250-8a759af5-f178-4d14-8e72-25cc4f448a34.png)

有很多原因可以导致丢帧， 一般主线程过多的UI绘制、大量的IO操作或是大量的计算操作占用CPU，都会导致App 界面卡顿。 一般主线程过多的UI绘制、大量的IO操作或是大量的计算操作占用CPU，导致App界面卡顿.

建议阅读：[android图形显示系统](https://blog.csdn.net/a740169405/article/details/70548443)

#### 3.1 Systrace 卡顿分析

​	Systrace 是Android平台提供的一款工具，用于记录短期内的设备活动。该工具会生成一份报告，其中汇总了 Android 内核中的数据，例如 CPU 调度程序、磁盘活动和应用线程。Systrace主要用来分析绘制性能方面的问 题。在发生卡顿时，通过这份报告可以知道当前整个系统所处的状态，从而帮助开发者更直观的分析系统瓶颈，改 进性能。 TraceView可以看出代码在运行时的一些具体信息，方法调用时长，次数，时间比率，了解代码运行过程的 效率问题，从而针对性改善代码。所以对于可能导致卡顿的耗时方法也可以通过TraceView检测。 要使用Systrace，需要先安装 Python2.7。安装完成后配置环境变量 path ，随后在命令行输入： python -- version 进行验证。

[Systrace具体使用](https://www.jianshu.com/p/e73768e66b8d) 

执行systrace可以选择配置自己感兴趣的category，常用的有：

| 标签   | 描述                                                         |
| ------ | ------------------------------------------------------------ |
| gfx    | Graphics 图形系统，包括SerfaceFlinger，VSYNC消息，Texture，RenderThread等 |
| input  | Input输入系统，按键或者触摸屏输入；分析滑动卡顿等            |
| view   | View绘制系统的相关信息，比如onMeasure，onLayout等；分析View绘制性能 |
| am     | ActivityManager调用的相关信息；分析Activity的启动、跳转      |
| dalvik | 虚拟机相关信息；分析虚拟机行为，如 GC停顿                    |
| sched  | CPU调度的信息，能看到CPU在每个时间段在运行什么线程，线程调度情况，锁信息。 |
| disk   | IO信息                                                       |
| wm     | WindowManager的相关信息                                      |
| res    | 资源加载的相关信息                                           |

其实Systrace对于应用开发者来说，能看的并不多。主要用于看是否丢帧，与丢帧时系统以及我们应用大致的一个 状态。我们在抓取systrace文件的时候，切记不要抓取太长时间，也不要太多不同操作。

```shell
python systrace.py -t 5 -o F:\Lance\optimizer\lsn2_jank\a.html gfx input view am dalvik sched wm disk res -a PackageName
```

Android 系统来说 绘制一帧 在16.6 左右 1000/60

打开抓取的html文件，可以看到我们应用存在非常严重的掉帧，不借助工具直接用肉眼看应用UI是看不出来的。如 果只是单独存在一个红色或者黄色的都是没关系的。关键是连续的红/黄色或者两帧间隔非常大那就需要我们去仔 细观察。按“W” 放大视图，在UIThread（主线程）上面有一条很细的线，表示线程状态。

![image-20220619183602657](https://user-images.githubusercontent.com/30100887/174489286-00bd172e-7a3d-4e6d-af0f-bf8de06ec5e7.png)

Systrace 会用不同的颜色来标识不同的线程状态, 在每个方法上面都会有对应的线程状态来标识目前线程所处的状 态。通过查看线程状态我们可以知道目前的瓶颈是什么, 是 CPU 执行慢还是因为 Binder 调用, 又或是进行 IO 操作, 又或是拿不到 CPU 时间片。 通过查看线程状态我们可以知道目前的瓶颈是什么, 是 CPU 执行慢还是因为 Binder 调用, 又或是进行 IO 操作, 又或是拿不到 CPU 时间片

线程状态主要有下面几个： 

- 绿色：表示正在运行。 
- 蓝色：表示可以运行，但是CPU在执行其他线程； 是否后台有太多的任务在跑？Runnable 状态的线程状态持续时间越长，则表示 cpu 的调度越忙，没有 及时处理到这个任务 没有及时处理是因为频率太低？ 
- 紫色：可中断的休眠 线程在遇到另一项内核操作（通常是内存管理）时被阻止。 但是实际从Android 9模拟器中拉取数据，遇到IO显示紫色，没有橙色状态显示。
- 橙色：不可中断的休眠 线程在遇到 I/O 操作时被阻止或正在等待磁盘操作完成。 



![image-20220619184827477](https://user-images.githubusercontent.com/30100887/174489293-7a5571a5-5312-4456-935f-9e6d338129b4.png)

在这里发现一个卡断的问题之一， ext4_write 一直在做文件操作，原因是RecycleView在滑动的过程中，又在做文件操作（我自己加的），造成了滑动卡顿，删除后就不再卡顿了。

### 3. 2 Trace API 

其实对于APP开发而言，使用systrace的帮助并不算非常大，大部分内容用于设备真机优化之类的系统开发人员观 察。systrace无法帮助应用开发者定位到准确的错误代码位置，我们需要凭借很多零碎的知识点与经验来猜测问题 原因。如果我们有了大概怀疑的具体的代码块或者有想了解的代码块执行时系统的状态，还可以结合 Trace API 打 标签。

 Android 提供了Trace API能够帮助我们记录收集自己应用中的一些信息 ： Trace.beginSection() 与 Trace.endSection();

```java
public class MainActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TraceCompat.beginSection("enjoy_launcher"); //Trace.beginSection()
		setContentView(R.layout.activity_main);
		TraceCompat.endSection(); //Trace.endSection()
	}
}

```

### 3.3 App层面监控卡顿 

systrace可以让我们了解应用所处的状态，了解应用因为什么原因导致的。若需要准确分析卡顿发生在什么函数， 资源占用情况如何，目前业界两种主流有效的app监控方式如下：

1、 利用UI线程的Looper打印的日志匹配； 

2、 使用Choreographer.FrameCallback

**Looper日志检测卡顿** 

Android主线程更新UI。如果界面1秒钟刷新少于60次，即FPS小于60，用户就会产生卡顿感觉。简单来说， Android使用消息机制进行UI更新，UI线程有个Looper，在其loop方法中会不断取出message，调用其绑定的 Handler在UI线程执行。如果在handler的dispatchMesaage方法里有耗时操作，就会发生卡顿。

```java
public static void loop() {
	//......
	for (;;) {
	//......
	Printer logging = me.mLogging;
	if (logging != null) {
		logging.println(">>>>> Dispatching to " + msg.target + " " +
		msg.callback + ": " + msg.what);
	}
	msg.target.dispatchMessage(msg);
	if (logging != null) {
		logging.println("<<<<< Finished to " + msg.target + " " + msg.callback);
		}
		//......
	}
}
```

只要检测 msg.target.dispatchMessage(msg) 的执行时间，就能检测到部分UI线程是否有耗时的操作。注意到这行 执行代码的前后，有两个logging.println函数，如果设置了logging，会分别打印出>>>>> Dispatching to和 <<<<< Finished to 这样的日志，这样我们就可以通过两次log的时间差值，来计算dispatchMessage的执行时 间，从而设置阈值判断是否发生了卡顿。

```java
public final class Looper {
	private Printer mLogging;
	public void setMessageLogging(@Nullable Printer printer) {
		mLogging = printer;
	}
}
public interface Printer {
	void println(String x);
}

```

Looper 提供了 setMessageLogging(@Nullable Printer printer) 方法，所以我们可以自己实现一个Printer，在 通过setMessageLogging()方法传入即可：

```java
public class BlockCanary {
	public static void install() {
		LogMonitor logMonitor = new LogMonitor();
		Looper.getMainLooper().setMessageLogging(logMonitor);
	}
}
public class LogMonitor implements Printer {
	private StackSampler mStackSampler;
	private boolean mPrintingStarted = false;
	private long mStartTimestamp;
	// 卡顿阈值
	private long mBlockThresholdMillis = 3000;
	//采样频率
	private long mSampleInterval = 1000;
	private Handler mLogHandler;
	public LogMonitor() {
		mStackSampler = new StackSampler(mSampleInterval);
		HandlerThread handlerThread = new HandlerThread("block-canary-io");
		handlerThread.start();
		mLogHandler = new Handler(handlerThread.getLooper());
}
	@Override
	public void println(String x) {
	//从if到else会执行 dispatchMessage，如果执行耗时超过阈值，输出卡顿信息
	if (!mPrintingStarted) {
		//记录开始时间
		mStartTimestamp = System.currentTimeMillis();
		mPrintingStarted = true;
		mStackSampler.startDump();
	} else {
		final long endTime = System.currentTimeMillis();
		mPrintingStarted = false;
	//出现卡顿
	if (isBlock(endTime)) {
		notifyBlockEvent(endTime);
	}
	mStackSampler.stopDump();
	}
}
private void notifyBlockEvent(final long endTime) {
	mLogHandler.post(new Runnable() {
	@Override
	public void run() {
	//获得卡顿时 主线程堆栈
		List<String> stacks = mStackSampler.getStacks(mStartTimestamp, endTime);
	for (String stack : stacks) {
		Log.e("block-canary", stack);
			}
		}
	});
}
private boolean isBlock(long endTime) {
		return endTime - mStartTimestamp > mBlockThresholdMillis;
	}
}
	public class StackSampler {
	public static final String SEPARATOR = "\r\n";
	public static final SimpleDateFormat TIME_FORMATTER =
	new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
	private Handler mHandler;
	private Map<Long, String> mStackMap = new LinkedHashMap<>();
	private int mMaxCount = 100;
	private long mSampleInterval;
	//是否需要采样
	protected AtomicBoolean mShouldSample = new AtomicBoolean(false);
	public StackSampler(long sampleInterval) {
		mSampleInterval = sampleInterval;
		HandlerThread handlerThread = new HandlerThread("block-canary-sampler");
		handlerThread.start();
		mHandler = new Handler(handlerThread.getLooper());
	}
/**
* 开始采样 执行堆栈
*/
public void startDump() {
//避免重复开始
	if (mShouldSample.get()) {
	return;
}
	mShouldSample.set(true);
	mHandler.removeCallbacks(mRunnable);
	mHandler.postDelayed(mRunnable, mSampleInterval);
}
public void stopDump() {
	if (!mShouldSample.get()) {
	return;

	其实这种方式也就是 BlockCanary 原理。
	Choreographer.FrameCallback
}
	mShouldSample.set(false);
	mHandler.removeCallbacks(mRunnable);
}
	public List<String> getStacks(long startTime, long endTime) {
	ArrayList<String> result = new ArrayList<>();
	synchronized (mStackMap) {
		for (Long entryTime : mStackMap.keySet()) {
			if (startTime < entryTime && entryTime < endTime) {
			result.add(TIME_FORMATTER.format(entryTime)
													+ SEPARATOR
														+ SEPARATOR
														+ mStackMap.get(entryTime));
			}
		}
	}
	return result;
}
private Runnable mRunnable = new Runnable() {
@Override
	public void run() {
		StringBuilder sb = new StringBuilder();
	StackTraceElement[] stackTrace = Looper.getMainLooper().getThread().getStackTrace();
	for (StackTraceElement s : stackTrace) {
		sb.append(s.toString()).append("\n");
	}
	synchronized (mStackMap) {
	//最多保存100条堆栈信息
		if (mStackMap.size() == mMaxCount) {
			mStackMap.remove(mStackMap.keySet().iterator().next());
		}
		mStackMap.put(System.currentTimeMillis(), sb.toString());
	}
	if (mShouldSample.get()) {
		mHandler.postDelayed(mRunnable, mSampleInterval);
			}
		}
	};
}
```



Choreographer.FrameCallback

Android系统每隔16ms发出VSYNC信号，来通知界面进行重绘、渲染，每一次同步的周期约为16.6ms，代表一帧 的刷新频率。通过Choreographer类设置它的FrameCallback函数，当每一帧被渲染时会触发回调 FrameCallback.doFrame (long frameTimeNanos) 函数。frameTimeNanos是底层VSYNC信号到达的时间戳 。

```java
public class ChoreographerHelper {
	public static void start() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			Choreographer.getInstance().postFrameCallback(new 								Choreographer.FrameCallback() {
				long lastFrameTimeNanos = 0;
				@Override
				public void doFrame(long frameTimeNanos) {
					//上次回调时间
					if (lastFrameTimeNanos == 0) {
					lastFrameTimeNanos = frameTimeNanos;
					Choreographer.getInstance().postFrameCallback(this);
					return;
				}
				long diff = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000;
				if (diff > 16.6f) {
					//掉帧数
					int droppedCount = (int) (diff / 16.6);
				}
					lastFrameTimeNanos = frameTimeNanos;
					Choreographer.getInstance().postFrameCallback(this);
				}
			});
		}
	}
}
```

通过 ChoreographerHelper 可以实时计算帧率和掉帧数，实时监测App页面的帧率数据，发现帧率过低，还可以自 动保存现场堆栈信息。

Looper比较适合在发布前进行测试或者小范围灰度测试然后定位问题，ChoreographerHelper适合监控线上环境 的 app 的掉帧情况来计算 app 在某些场景的流畅度然后有针对性的做性能优化。

## 4. 过度渲染

过度绘制是指系统在渲染单个帧的过程中多次在屏幕上绘制某一个像素。例如，如果我们有若干界面卡片堆叠在一 起，每张卡片都会遮盖其下面一张卡片的部分内容。但是，系统仍然需要绘制堆叠中的卡片被遮盖的部分。

GPU 过度绘制检查 手机开发者选项中能够显示过度渲染检查功能，通过对界面进行彩色编码来帮我们识别过度绘制。开启步骤如下： 

1. 进入开发者选项 (Developer Options)。
2. 找到调试 GPU 过度绘制(Debug GPU overdraw)。 
3. 在弹出的对话框中，选择显示过度绘制区域（Show overdraw areas）。

Android 将按如下方式为界面元素着色，以确定过度绘制的次数：

1. 真彩色：没有过度绘制 
2. 蓝色：过度绘制 1 次 
3. 绿色：过度绘制 2 次 享学课堂 
4. 粉色：过度绘制 3 次 
5. 红色：过度绘制 4 次或更多次

解决过度绘制问题 可以采取以下几种策略来减少甚至消除过度绘制： 

- 移除布局中不需要的背景。 默认情况下，布局没有背景，这表示布局本身不会直接渲染任何内容。但是，当布局具有背景时，其有 可能会导致过度绘制。 移除不必要的背景可以快速提高渲染性能。不必要的背景可能永远不可见，因为它会被应用在该视图上 绘制的任何其他内容完全覆盖。例如，当系统在父视图上绘制子视图时，可能会完全覆盖父视图的背 景。 
- 使视图层次结构扁平化。 可以通过优化视图层次结构来减少重叠界面对象的数量，从而提高性能。 降低透明度。 对于不透明的 view ，只需要渲染一次即可把它显示出来。但是如果这个 view 设置了 alpha 值，则至 少需要渲染两次。这是因为使用了 alpha 的 view 需要先知道混合 view 的下一层元素是什么，然后再 结合上层的 view 进行Blend混色处理。透明动画、淡入淡出和阴影等效果都涉及到某种透明度，这就会 造成了过度绘制。可以通过减少要渲染的透明对象的数量，来改善这些情况下的过度绘制。例如，如需 获得灰色文本，可以在 TextView 中绘制黑色文本，再为其设置半透明的透明度值。但是，简单地通过 用灰色绘制文本也能获得同样的效果，而且能够大幅提升性能


## 5. 网络优化

正常一条网络请求需要经过的流程是这样：

- DNS 解析，请求DNS服务器，获取域名对应的 IP 地址； 
- 与服务端建立连接，包括 tcp 三次握手，安全协议同步流程； 
- 连接建立完成，发送和接收数据，解码数据。

 这里有明显的三个优化点： 

- 直接使用 IP 地址，去除 DNS 解析步骤； 
- 不要每次请求都重新建立连接，复用连接或一直使用同一条连接(长连接)； 
- 压缩数据，减小传输的数据大小。

### 5.1 DNS优化

DNS（Domain Name System），它的作用是根据域名查出IP地址，它是HTTP协议的前提，只有将域名正确的解 析成IP地址后，后面的HTTP流程才能进行。



DNS 完整的解析流程很长，会先从本地系统缓存取，若没有就到最近的 DNS 服务器取，若没有再到主域名服务器 取，每一层都有缓存，但为了域名解析的实时性，每一层缓存都有过期时间。

传统的DNS解析机制有几个缺点： 

- 缓存时间设置得长，域名更新不及时，设置得短，大量 DNS 解析请求影响请求速度；
- 域名劫持，容易被中间人攻击，或被运营商劫持，把域名解析到第三方 IP 地址，据统计劫持率会达到7%； 
- DNS 解析过程不受控制，无法保证解析到最快的IP；
- 一次请求只能解析一个域名。

为了解决这些问题，就有了 HTTPDNS，原理很简单，就是自己做域名解析的工作，通过 HTTP 请求后台去拿到域 名对应的 IP 地址，直接解决上述所有问题。

HTTPDNS的好处总结就是： 

- Local DNS 劫持：由于 HttpDns 是通过 IP 直接请求 HTTP 获取服务器 A 记录地址，不存在向本地运营商询问 domain 解析过程，所以从根本避免了劫持问题。 
- DNS 解析由自己控制，可以确保根据用户所在地返回就近的 IP 地址，或根据客户端测速结果使用速度最快的 IP； 一次请求可以解析多个域名。 
- ......

接入文档 ：

[产品优势 - HTTPDNS - 阿里云 (aliyun.com)](https://help.aliyun.com/document_detail/30103.html?spm=a2c4g.11186623.6.543.7eee78bc3kDYhO)

1. 配置仓库

```groovy
maven {	url 'http://maven.aliyun.com/nexus/content/repositries/release/'}
```

2. 加入依赖

   ```groovy
   implemntation ('com.aliyun.ams:alicloud-android-httpdns:1.3.3@aar') {	transitive true	exlude group: 'com.aliyun.ams', moudle: 'alicloud-android-utdid'}implementation 'com.aliyun.ams:aliclound-android-utdid:1.1.5.3', 
   ```

3. 在network 模块 下 对Retrofit 进行配

   1. ```java
      protected Retrofit getRetrofit(class service) {	...................    retrofitBuilder.Client(getOkhttpClient());}getOkhttpClient() {    .............        // 把自定义得DNS注册进去        okhttpClientBuilder.dns(new AlibabaDns(iNetworkRequiredInfo).getApplicationContext());}public class AlibabaDns implements Dns {        private HttpDnsService httpdns;        public AlibabaDns(Context context) {        httpdns = HttpDns = httpDns.getService(context, '169929');    }        public List<InetAddress> lookup(String hostname) throws unknowHostException {        // 通过异步解析接口获取IP        String ip = httpdns.getIpByHostAsync(hostName);        if(iP != null) {            List<InetAddress> inetAddress = Arrays.asList(InetAddress.getAllByName(ip));            return inetAddresses;        }        return Dns.SYSTEM.lookUp(hostname);    }}================= 原理 首先使用固定IP 进行Http 请求 private static final String[] b = new String[] {"203.107.1.97", "203.107.1.100", "httpdns-sc.aliyundns.com"};    
      ```

      把要解析的域名 传给服务器。

### 5.2 优化连接

连接建立耗时的问题，这里主要的优化思路是复用连接，不用每次请求都重新建立连接，如何更有效 率地复用连接，可以说是网络请求速度优化里最主要的点了。

【keep-alive】： HTTP 协议里有个 keep-alive，HTTP1.1默认开启，一定程度上缓解了每次请求都要进行TCP三 次握手建立连接的耗时。原理是请求完成后不立即释放连接，而是放入连接池中，若这时有另一个请求要发出，请 求的域名和端口是一样的，就直接拿出连接池中的连接进行发送和接收数据，少了建立连接的耗时。 实际上现在无 论是客户端还是浏览器都默认开启了keep-alive，对同个域名不会再有每发一个请求就进行一次建连的情况，纯短 连接已经不存在了。

但有 keep-alive 的连接一次只能发送接收一个请求，在上一个请求处理完成之前，无法接受新的请求。若同时发 起多个请求，就有两种情况： 

- 若串行发送请求，可以一直复用一个连接，但速度很慢，每个请求都要等待上个请求完成再进行发送。 
- 若并行发送请求，那么只能每个请求都要进行tcp三次握手建立新的连接。

​	对并行请求的问题，新一代协议 HTTP2 提出了多路复用去解决。 HTTP2 的多路复用机制一样是复用连接，但它复 用的这条连接支持同时处理多条请求，所有请求都可以并发在这条连接上进行，也就解决了上面说的并发请求需要 建立多条连接带来的问题。

​	多路复用把在连接里传输的数据都封装成一个个stream，每个stream都有标识，stream的发送和接收可以是乱序 的，不依赖顺序，也就不会有阻塞的问题，接收端可以根据stream的标识去区分属于哪个请求，再进行数据拼接， 得到最终数据。 Android 的开源网络库OKhttp默认就会开启 keep-alive ，并且在Okhttp3以上版本也支持了 HTTP2。

### 5.3 数据压缩

传输数据大小的问题。数据对请求速度的影响分两方面，一是压缩率，二是解压序列化反序列化的速 度。目前最流行的两种数据格式是 json 和 protobuf，json 是字符串，protobuf 是二进制，即使用各种压缩算法 压缩后，protobuf 仍会比 json 小，数据量上 protobuf 有优势，序列化速度 protobuf 也有一些优势 。

除了选择不同的序列化方式（数据格式）之外，Http可以对内容（也就是body部分）进行编码，可以采用gzip这 样的编码，从而达到压缩的目的。

在OKhttp的 BridgeInterceptor 中会自动为我们开启gzip解压的支持。

```java
boolean transparentGzip = false;if (userRequest.header("Accept-Encoding") == null && userRequest.header("Range") == null) {	transparentGzip = true;	requestBuilder.header("Accept-Encoding", "gzip");}
```

如果服务器响应头存在： Content-Encodin:gzip

```java
/服务器响应 Content-Encodin:gzip 并且有响应body数据if (transparentGzip	&& "gzip".equalsIgnoreCase(networkResponse.header("Content-Encoding"))	&& HttpHeaders.hasBody(networkResponse)) {	GzipSource responseBody = new GzipSource(networkResponse.body().source());	Headers strippedHeaders = networkResponse.headers().newBuilder()		.removeAll("Content-Encoding")		.removeAll("Content-Length")		.build();	responseBuilder.headers(strippedHeaders);	String contentType = networkResponse.header("Content-Type");	responseBuilder.body(new RealResponseBody(contentType, -1L, Okio.buffer(responseBody)));}
```

客户端也可以发送压缩数据给服务端，通过代码将请求数据压缩，并在请求中加入 Content-Encodin:gzip 即可

```java
private RequestBody gzip(final RequestBody body) {	return new RequestBody() {		@Override		public MediaType contentType() {			return body.contentType();		}		@Override		public long contentLength() {			return -1; // We don't know the compressed length in advance!		}		@Override		public void writeTo(BufferedSink sink) throws IOException {			BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));			body.writeTo(gzipSink);			gzipSink.close();		}	};    public RequestBody getGzipRequest(String body) {	RequestBody request = null;	try {		request = RequestBody.create(		MediaType.parse("application/octet-stream"),compress(body)		);		} catch (IOException e) {			e.printStackTrace();	}	return request;}
```

1、使用webp代替png/jpg 

2、不同网络的不同图片下发，如（对于原图是300x300的图片）： 

2/3G使用低清晰度图片：使用100X100的图片;

- 4G再判断信号强度为强则使用使用300X300的图片，
- 为中等则使用200x200，信号弱则使用100x100图片; 
- WiFi网络：直接下发300X300的图片 

3、http开启缓存 / 首页数据加入缓存

## 6. Crash监控

Crash（应用崩溃）是由于代码异常而导致 App 非正常退出，导致应用程序无法继续使用，所有工作都 停止的现象。发生 Crash 后需要重新启动应用（有些情况会自动重启），而且不管应用在开发阶段做得 多么优秀，也无法避免 Crash 发生，特别是在 Android 系统中，系统碎片化严重、各 ROM 之间的差 异，甚至系统Bug，都可能会导致Crash的发生。 在 Android 应用中发生的 Crash 有两种类型，Java 层的 Crash 和 Native 层 Crash。这两种Crash 的监 控和获取堆栈信息有所不同。

### 6.1 Java Crash

Java的Crash监控非常简单，Java中的Thread定义了一个接口： UncaughtExceptionHandler ；用于 处理未捕获的异常导致线程的终止（注意：catch了的是捕获不到的），当我们的应用crash的时候，就 会走 UncaughtExceptionHandler.uncaughtException ，在该方法中可以获取到异常的信息，我们通 过 Thread.setDefaultUncaughtExceptionHandler 该方法来设置线程的默认异常处理器

```java
package com.enjoy.crash;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;


import com.tencent.mars.xlog.Log;
import com.tencent.mars.xlog.Xlog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String FILE_NAME_SUFFIX = ".trace";
    private static Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler;
    private static Context context;


    public static void init(Context applicationContext) {
        context = applicationContext;
        defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler());
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        try {
            File file = dealException(t, e);

        } catch (Exception exception) {

        } finally {
            if (defaultUncaughtExceptionHandler != null) {
                defaultUncaughtExceptionHandler.uncaughtException(t, e);
            }
        }
    }

    private File dealException(Thread thread, Throwable throwable) throws JSONException, IOException, PackageManager.NameNotFoundException {
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        //私有目录，无需权限
        File f = new File(context.getExternalCacheDir().getAbsoluteFile(), "crash_info");
        if (!f.exists()) {
            f.mkdirs();
        }
        File crashFile = new File(f, time + FILE_NAME_SUFFIX);
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(crashFile)));
        pw.println(time);
        pw.println("Thread: " + thread.getName());
        pw.println(getPhoneInfo());
        throwable.printStackTrace(pw); //写入crash堆栈
        pw.flush();
        pw.close();
        return crashFile;
    }

    private String getPhoneInfo() throws PackageManager.NameNotFoundException {
        PackageManager pm = context.getPackageManager();
        PackageInfo pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
        StringBuilder sb = new StringBuilder();
        //App版本
        sb.append("App Version: ");
        sb.append(pi.versionName);
        sb.append("_");
        sb.append(pi.versionCode + "\n");

        //Android版本号
        sb.append("OS Version: ");
        sb.append(Build.VERSION.RELEASE);
        sb.append("_");
        sb.append(Build.VERSION.SDK_INT + "\n");

        //手机制造商
        sb.append("Vendor: ");
        sb.append(Build.MANUFACTURER + "\n");

        //手机型号
        sb.append("Model: ");
        sb.append(Build.MODEL + "\n");

        //CPU架构
        sb.append("CPU: ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sb.append(Arrays.toString(Build.SUPPORTED_ABIS));
        } else {
            sb.append(Build.CPU_ABI);
        }
        return sb.toString();
    }
}

```

### Native crash

相对于Java的Crash，NDK的错误无疑更加让人头疼，特别是对初学NDK的同学，不说监控，就算是错 误堆栈都不知道怎么看。

Linux信号机制：

信号机制是Linux进程间通信的一种重要方式，Linux信号一方面用于正常的进程间通信和同步，另一方 面它还负责监控系统异常及中断。当应用程序运行异常时，Linux内核将产生错误信号并通知当前进 程。当前进程在接收到该错误信号后，可以有三种不同的处理方式。 忽略该信号； 捕捉该信号并执行对应的信号处理函数（信号处理程序）； 执行该信号的缺省操作（如终止进程）； 当Linux应用程序在执行时发生严重错误，一般会导致程序崩溃。其中，Linux专门提供了一类crash信 号，在程序接收到此类信号时，缺省操作是将崩溃的现场信息记录到核心文件，然后终止进程。 常见崩溃信号列表：

| 信号    | 描述                           |
| ------- | ------------------------------ |
| SIGSEGV | 内存引用无效。                 |
| SIGBUS  | 访问内存对象的未定义部分。     |
| SIGFPE  | 算术运算错误，除以零。         |
| SIGILL  | 非法指令，如执行垃圾或特权指令 |
| SIGSYS  | 糟糕的系统调用                 |
| SIGXCPU | 超过CPU时间限制。              |
| SIGXFSZ | 文件大小限制。                 |

一般的出现崩溃信号，Android系统默认缺省操作是直接退出我们的程序。但是系统允许我们给某一个 进程的某一个特定信号注册一个相应的处理函数（signal），即对该信号的默认处理动作进行修改。因 此NDK Crash的监控可以采用这种信号机制，捕获崩溃信号执行我们自己的信号处理函数从而捕获NDK Crash。

### **墓碑**

> 此处了解即可，普通应用无权限读取墓碑文件，墓碑文件位于路径/data/tombstones/下。解析墓
> 碑文件与后面的breakPad都可使用 addr2line 工具。

Android本机程序本质上就是一个Linux程序，当它在执行时发生严重错误，也会导致程序崩溃，然后产 生一个记录崩溃的现场信息的文件，而这个文件在Android系统中就是 tombstones 墓碑文件。

### BreakPad

Google breakpad是一个跨平台的崩溃转储和分析框架和工具集合，其开源地址是：https://github.co m/google/breakpad。breakpad在Linux中的实现就是借助了Linux信号捕获机制实现的。因为其实现 为C++，因此在Android中使用，必须借助NDK工具。

#### 引入项目

将Breakpad源码下载解压，首先查看README.ANDROID文件

![image-20220625132515164](https://user-images.githubusercontent.com/30100887/175760079-cc06e184-6fb9-4f9c-92bb-fcc24e22be42.png)

打开 README.ANDROID

![image-20220625132842242](https://user-images.githubusercontent.com/30100887/175760074-f7cfef89-e74e-4d87-a4af-2f2f5eca889b.png)

按照文档中的介绍，如果我们使用Android.mk 非常简单就能够引入到我们工程中，但是目前NDK默认 的构建工具为：CMake，因此我们做一次移植。查看android/google_breakpad/Android.mk

对照Android.mk文件，我们在自己项目的cpp(工程中C/C++源码)目录下创建breakpad目录，并将下载 的breakpad源码根目录下的src目录全部复制到我们的项目中：

![image-20220625132947513](https://user-images.githubusercontent.com/30100887/175760066-a8e29561-c433-42c0-99fb-45980ccb8f0e.png)

接下来在breakpad目录下创建CMakeList.txt文件:

```cmake
cmake_minimum_required(VERSION 3.4.1)

set(BREAKPAD_ROOT ${CMAKE_CURRENT_SOURCE_DIR})
#对应android.mk中的 LOCAL_C_INCLUDES
include_directories(src src/common/android/include)
#开启arm汇编支持，因为在源码中有 .S文件（汇编源码）
enable_language(ASM)
#生成 libbreakpad.a 并指定源码，对应android.mk中 LOCAL_SRC_FILES+LOCAL_MODULE
add_library(breakpad STATIC
        src/client/linux/crash_generation/crash_generation_client.cc
        src/client/linux/dump_writer_common/thread_info.cc
        src/client/linux/dump_writer_common/ucontext_reader.cc
        src/client/linux/handler/exception_handler.cc
        src/client/linux/handler/minidump_descriptor.cc
        src/client/linux/log/log.cc
        src/client/linux/microdump_writer/microdump_writer.cc
        src/client/linux/minidump_writer/linux_dumper.cc
        src/client/linux/minidump_writer/linux_ptrace_dumper.cc
        src/client/linux/minidump_writer/minidump_writer.cc
        src/client/minidump_file_writer.cc
        src/common/convert_UTF.cc
        src/common/md5.cc
        src/common/string_conversion.cc
        src/common/linux/breakpad_getcontext.S
        src/common/linux/elfutils.cc
        src/common/linux/file_id.cc
        src/common/linux/guid_creator.cc
        src/common/linux/linux_libc_support.cc
        src/common/linux/memory_mapped_file.cc
        src/common/linux/safe_readlink.cc)
#链接 log库，对应android.mk中 LOCAL_EXPORT_LDLIBS
target_link_libraries(breakpad log)
```

在cpp目录下（breakpad同级）还有一个CMakeList.txt文件，它的内容是：

```cmake
cmake_minimum_required(VERSION 3.4.1)

#引入breakpad的头文件（api的定义）

include_directories(breakpad/src breakpad/src/common/android/include)
#引入breakpad的cmakelist，执行并生成libbreakpad.a (api的实现，类似java的jar包)
add_subdirectory(breakpad)
#生成libbugly.so 源码是：ndk_crash.cpp(我们自己的源码，要使用breakpad)
add_library(
        bugly
        SHARED
        ndk_crash.cpp)


target_link_libraries(
        bugly
        breakpad
        log)
```

此时执行编译，会在 #include "third_party/lss/linux_syscall_support.h" 报错，无法找到头 文件。此文件从：https://chromium.googlesource.com/external/linux-syscall-support/+/refs/head s/master 下载（需要翻墙）放到工程对应目录即可。

ndk_crash.cpp 源文件中的实现为：

```C++
#include <jni.h>
#include <android/log.h>
#include "breakpad/src/client/linux/handler/minidump_descriptor.h"
#include "breakpad/src/client/linux/handler/exception_handler.h"

bool DumpCallback(const google_breakpad::MinidumpDescriptor &descriptor,
                  void *context,
                  bool succeeded) {
    __android_log_print(ANDROID_LOG_ERROR, "ndk_crash", "Dump path: %s", descriptor.path());
    //如果回调返回true，Breakpad将把异常视为已完全处理，禁止任何其他处理程序收到异常通知。
    //如果回调返回false，Breakpad会将异常视为未处理，并允许其他处理程序处理它。
    return false;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_enjoy_crash_CrashReport_initBreakpad(JNIEnv *env, jclass type, jstring path_) {
    const char *path = env->GetStringUTFChars(path_, 0);
//开启crash监控
    google_breakpad::MinidumpDescriptor descriptor(path);
    static google_breakpad::ExceptionHandler eh(descriptor, NULL, DumpCallback, NULL, true, -1);
    env->ReleaseStringUTFChars(path_, path);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_enjoy_crash_CrashReport_testNativeCrash(JNIEnv *env, jclass clazz) {

    int *i = NULL;
    *i = 1;
}
```

```java
import java.io.File;

public class CrashReport {

    static {
        System.loadLibrary("bugly");
    }


    public static void init(Context context) {
        Context applicationContext = context.getApplicationContext();
        CrashHandler.init(applicationContext);
        File file = new File(context.getExternalCacheDir(), "native_crash");
        if (!file.exists()) {
            file.mkdirs();
        }
        initBreakpad(file.getAbsolutePath());
    }

    private static native void initBreakpad(String path);

    public static native void testNativeCrash();

    public static int testJavaCrash() {
        return 1 / 0;
    }

}

```

此时，如果出现NDK Crash，会在我们指定的目 录： /sdcard/Android/Data/[packageName]/cache/native_crash 下生成NDK Crash信息文件。

Crash解析

采集到的Crash信息记录在minidump文件中。minidump是由微软开发的用于崩溃上传的文件格式。我 们可以将此文件上传到服务器完成上报，但是此文件没有可读性可言，要将文件解析为可读的崩溃堆栈 需要按照breakpad文档编译 minidump_stackwalk 工具，而Windows系统编译个人不会。不过好在， 无论你是 Mac、windows还是ubuntu在 Android Studio 的安装目录下的 bin\lldb\bin 里面就存在一 个对应平台的 minidump_stackwalk 。


使用这里的工具执行：

```
minidump_stackwalk xxxx.dump > crash.txt
```

打开 crash.txt 内容为：

```shell
CPU: x86 // abi类型
GenuineIntel family 6 model 31 stepping 1
3 CPUs
GPU: UNKNOWN
Crash reason: SIGSEGV //内存引用无效 信号
Crash address: 0x0
Process uptime: not available
Thread 0 (crashed) //crashed：出现crash的线程
0 libbugly.so + 0x1feab //crash的so与寄存器信息
eip = 0xd5929eab esp = 0xffa85f30 ebp = 0xffa85f38 ebx = 0x0000000c
esi = 0xd71a3f04 edi = 0xffa86128 eax = 0xffa85f5c ecx = 0xefb19400
edx = 0x00000000 efl = 0x00210286
Found by: given as instruction pointer in context
1 libart.so + 0x5f6a18
eip = 0xef92ea18 esp = 0xffa85f40 ebp = 0xffa85f60
Found by: previous frame's frame pointer
Thread 1

```

接下来使用 Android NDK 里面提供的 addr2line 工具将寄存器地址转换为对应符号。addr2line 要用和 自己 so 的 ABI 匹配的目录，同时需要使用有符号信息的so(一般debug的就有)。

 Android\Sdk\ndk\21.3.6528147\toolchains\x86-4.9\prebuilt\windows-x86_64\bin\i686-linux-android-addr2line.exe

i686-linux-android-addr2line.exe -f -C -e libbugly.so 0x1feab

![image-20220625134030864](https://user-images.githubusercontent.com/30100887/175760054-ae585a0a-577f-4fe5-b0b3-9ec88f103c5f.png)

![image-20220625134049105](https://user-images.githubusercontent.com/30100887/175760046-d5134d54-ad26-4353-8ab3-7acb4af6352b.png)


