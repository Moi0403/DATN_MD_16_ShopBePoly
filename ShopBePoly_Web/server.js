const express = require('express');
const mongoose = require('mongoose');
const router = express.Router();
const app = express();
const cors = require('cors');
const port = 3000;

const bodyParser = require('body-parser');
app.use(bodyParser.json());
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use('/uploads', express.static('uploads', {
    setHeaders: res => res.set('Cache-Control', 'no-store')
}));
const fs = require('fs');



const productModel = require('./Database/productModel');
const COMOMJS = require('./Database/COMOM');
const userModel = require('./Database/userModel');
const cartModel = require('./Database/cartModel');
const commentModel = require('./Database/commentModel');
const categoryModel = require('./Database/categoryModel');
const orderModel = require('./Database/orderModel');
const favoriteModel = require('./Database/favoriteModel');
const Favorite = favoriteModel;
const messageModel = require('./Database/messageModel');
const notificationModel = require('./Database/notificationModel');
const sendEmail = require('./Database/sendEmail');
const VerifyCode = require('./Database/VerifyCode')

const uri = COMOMJS.uri;

// Kết nối MongoDB
mongoose.connect(uri, {
    useNewUrlParser: true,
    useUnifiedTopology: true,
    serverSelectionTimeoutMS: 5000 // để không bị treo nếu không kết nối được
})
    .then(() => console.log('Connected to MongoDB'))
    .catch((err) => console.error('MongoDB connection error:', err));

// Khởi động server
app.listen(port, () => {
    console.log(`Server is running on port ${port}`);
});

const multer = require('multer');
const path = require('path');

const storage = multer.diskStorage({
    destination: (req, file, cb) => cb(null, 'uploads/'),
    filename: async (req, file, cb) => {
        const ext = path.extname(file.originalname);
        const userId = req.params.id;
        const fileName = `${userId}${ext}`;

        // Xóa ảnh cũ nếu tồn tại
        const user = await userModel.findById(userId);
        if (user?.avt_user && user.avt_user !== fileName) {
            const oldPath = path.join(__dirname, 'uploads', user.avt_user);
            if (fs.existsSync(oldPath)) {
                fs.unlinkSync(oldPath);
            }
        }

        cb(null, fileName);
    }
});


const storageProduct = multer.diskStorage({
    destination: (req, file, cb) => cb(null, 'uploads/'),
    filename: (req, file, cb) => {
        const ext = path.extname(file.originalname);
        const uniqueName = `${Date.now()}-${Math.round(Math.random() * 1e9)}${ext}`;
        cb(null, uniqueName);
    }
});
const uploadProduct = multer({ storage: storageProduct });

const upload = multer({ storage });

router.get('/notifications/:userId', async (req, res) => {
    const { userId } = req.params;
    try {
        const notifications = await notificationModel.find({ userId }).sort({ createdAt: -1 });
        res.json(notifications);
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: 'Lỗi khi lấy thông báo' });
    }
});

// API cập nhật avatar
router.post('/upload-avatar/:id', upload.single('avt_user'), async (req, res) => {
    const userId = req.params.id;

    if (!req.file) {
        return res.status(400).json({ success: false, message: 'Không có file được tải lên.' });
    }

    try {

        const avatarFileName = req.file.filename;

        const updatedUser = await userModel.findByIdAndUpdate(
            userId,
            { avt_user: avatarFileName },
            { new: true }
        );

        if (!updatedUser) {
            return res.status(404).json({ success: false, message: 'Người dùng không tồn tại.' });
        }

        res.status(200).json({
            success: true,
            message: 'Cập nhật ảnh đại diện thành công.',
            avt_user: updatedUser.avt_user
        });
    } catch (err) {
        res.status(500).json({ success: false, message: 'Lỗi server', error: err.message });
    }
});




app.get('/ds_product', async (req, res) => {
    try {
        const products = await productModel.find();
        console.log(products);
        res.json(products); // trả kết quả về client
    } catch (err) {
        console.error('Error fetching products:', err);
        res.status(500).json({ error: 'Lỗi khi lấy danh sách sản phẩm' });
    }
});
app.use('/api/users', router);
app.use('/api', router);

// lấy ds product 'http://localhost:3000/api/list_product'
router.get('/list_product', async (req, res) => {
    try {
        const products = await productModel.find().populate('id_category');
        res.json(products);
    } catch (error) {
        console.error('Lỗi khi lấy danh sách sản phẩm:', error);
        res.status(500).send('Lỗi server khi lấy sản phẩm');
    }
});

// thêm product 'http://localhost:3000/api/add_product'
router.post('/add_product', uploadProduct.any(), async (req, res) => {
    try {
        const files = req.files || [];
        const body = req.body;

        let variations = [];
        if (body.variations) {
            try {
                variations = JSON.parse(body.variations);
            } catch (err) {
                return res.status(400).json({ message: 'Lỗi định dạng variations' });
            }
        }

        const avt_imgpro = files.find(f => f.fieldname === 'avt_imgpro');

        variations.forEach((variation, index) => {
            const fieldName = `variationImages-${index}`;
            const matchedFiles = files.filter(f => f.fieldname === fieldName);

            variation.list_imgproduct = matchedFiles.map(f => f.filename);
            variation.image = matchedFiles[0]?.filename || '';

            if (!variation.list_imgproduct || variation.list_imgproduct.length === 0) {
                variation.list_imgproduct = []; // Không fallback về avt
            }
        });


        const mergedImages = [];

        // Gộp ảnh từ variations vào danh sách ảnh chính
        variations.forEach(variation => {
            if (Array.isArray(variation.list_imgproduct)) {
                variation.list_imgproduct.forEach(img => {
                    if (img && !mergedImages.includes(img)) {
                        mergedImages.push(img);
                    }
                });
            }
            if (!variation.list_imgproduct || variation.list_imgproduct.length === 0) {
                variation.list_imgproduct = [];
            }


        });

        // Gộp thêm ảnh từ field list_imgproduct (ảnh phụ không theo màu)
        const additionalImgs = files.filter(f => f.fieldname === 'list_imgproduct');
        additionalImgs.forEach(f => {
            if (!mergedImages.includes(f.filename)) {
                mergedImages.push(f.filename);
            }
        });

        const newProduct = new productModel({
            nameproduct: body.name_pro,
            id_category: body.category_pro,
            price: body.price_pro,
            description: body.mota_pro,
            avt_imgproduct: avt_imgpro?.filename || '',
            list_imgproduct: mergedImages,
            variations: variations
        });

        await newProduct.save();
        console.log('✅ Thêm sản phẩm thành công');
        const allProducts = await productModel.find().populate('id_category');
        res.status(200).json(allProducts);
    } catch (error) {
        console.error('❌ Thêm sản phẩm thất bại:', error);
        res.status(500).json({ message: 'Lỗi server', error: error.message });
    }
});



// sửa product 'http://localhost:3000/api/up_product/ id'
router.put('/up_product/:id', async (req, res) => {
    try {
        const id = req.params.id;
        const data = req.body;

        const kq = await productModel.findByIdAndUpdate(id, data, { new: true });

        if (kq) {
            console.log('Sửa thành công');
            let pro = await productModel.find();
            res.send(pro);
        } else {
            res.send('Không tìm thấy sản phẩm để sửa');
        }
    } catch (error) {
        res.send('Lỗi khi sửa')
    }
})

// xóa sản phẩm 'http://localhost:3000/api/del_product/ id'
router.delete('/del_product/:id', async (req, res) => {
    try {
        let id = req.params.id;
        const kq = await productModel.deleteOne({ _id: id });
        if (kq) {
            console.log('Xóa sản phẩm thành công');
            let pro = await productModel.find();
            res.send(pro);
        } else {
            res.send('Xóa sản phẩm không thành công');
        }
    } catch (error) {
        console.error('Lỗi khi xóa:', error);
        res.status(500).json({ error: 'Lỗi server khi xóa sản phẩm' });
    }
})

// Tìm kiếm san pham 'http://localhost:3000/api/search_product'
router.get('/search_product', async (req, res) => {
    try {
        const keyword = req.query.q;
        if (!keyword || keyword.trim() === "") {
            // Trả về mảng rỗng thay vì toàn bộ sản phẩm
            return res.json([]);
        }
        const results = await productModel.find({
            nameproduct: { $regex: keyword, $options: 'i' }
        }).populate('id_category');
        res.json(results);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

//User
// lấy ds user 'http://localhost:3000/api/list_user'
router.get('/list_user', async (req, res) => {
    try {
        const users = await userModel.find().select('-password'); // Trả về tất cả trường trừ password
        res.json(users);
    } catch (error) {
        console.error('Lỗi khi lấy danh sách user:', error);
        res.status(500).json({ error: 'Lỗi server' });
    }
});

// thêm user 'http://localhost:3000/api/add_user'
router.post('/add_user', upload.fields([
    { name: 'avt_user', maxCount: 1 },
]), async (req, res) => {

    try {
        const files = req.files;
        const body = req.body;

        const newUser = await userModel.create({
            username: body.username_user,
            password: body.password_user,
            name: body.name_user,
            email: body.email_user,
            phone_number: body.phone_user,
            birthday: body.birthday_user,
            gender: body.gender_user,
            avt_user: (files && files.avt_user && files.avt_user[0]) ? files.avt_user[0].filename : '',
            role: body.role_user
        });
        await newUser.save();
        console.log('Thêm tài khoản thành công');
        const allUsers = await userModel.find();
        res.json(allUsers);
    } catch (error) {
        console.error('Thêm tài khoản thất bại:', error);
        res.status(500).send('Lỗi server');
    }

})

// sửa user 'http://localhost:3000/api/up_user/ id'
router.put('/up_user/:id', upload.single('avt'), async (req, res) => {
    try {
        const id = req.params.id;
        const data = req.body;
        console.log('Dữ liệu nhận được từ app:', data); // Thêm dòng này

        if (data.birthday_user) {
            data.birthday = data.birthday_user;
            delete data.birthday_user;
        }
        if (data.gender_user) {
            data.gender = data.gender_user;
            delete data.gender_user;
        }
        if (req.file) {
            data.avt = req.file.path; // Lưu đường dẫn file vào trường avatar
        }
        const kq = await userModel.findByIdAndUpdate(id, data, { new: true });
        if (kq) {
            console.log('Sửa thành công');
            let usr = await userModel.find();
            res.send(usr);
        } else {
            res.send('Không tìm thấy người dùng để sửa');
        }
    } catch (error) {
        res.send('Lỗi khi sửa')
    }
})

router.put('/up_user/:id', upload.single('avatar'), async (req, res) => {
    try {
        const id = req.params.id;
        const data = req.body;
        if (req.file) {
            data.avatar = req.file.path; // Lưu đường dẫn file vào trường avatar
        }
        const kq = await userModel.findByIdAndUpdate(id, data, { new: true });
        if (kq) {
            res.json(kq);
        } else {
            res.status(404).send('Không tìm thấy người dùng để sửa');
        }
    } catch (error) {
        res.status(500).send('Lỗi khi sửa');
    }
});

// xóa user 'http://localhost:3000/api/del_user/ id'
router.delete('/del_user/:id', async (req, res) => {
    try {
        let id = req.params.id;
        const kq = await userModel.deleteOne({ _id: id });
        if (kq) {
            console.log('Xóa thành công');
            let usr = await userModel.find();
            res.send(usr);
        } else {
            res.send('Xóa không thành công');
        }
    } catch (error) {
        console.error('Lỗi khi xóa:', error);
        res.status(500).json({ error: 'Lỗi server' });
    }
})
// Đăng ký
router.post('/register', async (req, res) => {
    let { username, password, name, email, phone_number, birthday, gender } = req.body;

    // Chuyển phone_number sang Number (nếu client gửi chuỗi)
    phone_number = Number(phone_number);

    try {
        // Kiểm tra username đã tồn tại chưa
        const existingUser = await userModel.findOne({ username });
        if (existingUser) {
            return res.status(400).json({ message: 'Tên người dùng đã tồn tại' });
        }

        // Tạo người dùng mới
        const newUser = await userModel.create({
            username,
            password,
            name,
            email,
            phone_number,
            birthday: birthday || null,
            gender: gender || '',
            avt_user: "",
            role: 0
        });

        res.status(201).json({
            message: 'Đăng ký thành công',
            user: {
                id: newUser._id,
                username: newUser.username,
                name: newUser.name,
                role: newUser.role
            }
        });

    } catch (error) {
        console.error('Lỗi đăng ký:', error);
        res.status(500).json({ message: 'Lỗi server khi đăng ký' });
    }
});


// Đăng nhập
router.post('/login', async (req, res) => {
    const { username, password } = req.body;

    try {
        const user = await userModel.findOne({ username });

        if (!user) {
            return res.status(401).json({ message: 'Tài khoản không tồn tại' });
        }

        if (user.password !== password) {
            return res.status(401).json({ message: 'Sai mật khẩu' });
        }

        res.status(200).json({
            message: 'Đăng nhập thành công',
            user: {
                id: user._id,
                username: user.username,
                name: user.name,
                role: user.role
            }
        });

    } catch (error) {
        console.error('Lỗi khi đăng nhập:', error);
        res.status(500).json({ message: 'Lỗi server khi đăng nhập' });
    }
});


// Lấy giỏ hàng http://localhost:3000/api/:useId
router.get('/list_cart/:userId', async (req, res) => {
    try {
        const cartItems = await cartModel.find({ id_user: req.params.userId })
            .populate({
                path: 'id_product',
                populate: { path: 'id_category' }  // <- thêm dòng này để populate category bên trong product
            });
        res.json(cartItems);
    } catch (error) {
        console.error('Lỗi khi lấy giỏ hàng:', error);
        res.status(500).json({ error: 'Lỗi khi lấy giỏ hàng' });
    }
});


// thêm giỏ hàng http://localhost:3000/api/add_cart
router.post('/add_cart', async (req, res) => {
    try {
        const {
            id_user,
            id_product,
            img_cart,
            quantity,
            price,
            size,
            color,
            status
        } = req.body;

        // Tính tổng tiền
        const total = quantity * price;

        const newCartItem = new cartModel({
            id_user,
            id_product,
            img_cart,
            quantity,
            price,
            size,
            color,
            total,
            status
        });

        const savedCart = await newCartItem.save();

        res.status(201).json({
            message: 'Thêm sản phẩm vào giỏ hàng thành công',
            data: savedCart
        });
    } catch (err) {
        res.status(500).json({
            message: 'Lỗi khi thêm vào giỏ hàng',
            error: err.message
        });
    }
});

// cập nhập só lượng trong giỏ hàng http://localhost:3000/api/up_cart/:idCart
router.put('/up_cart/:idCart', async (req, res) => {

    try {
        await mongoose.connect(uri);
        const cartId = req.params.idCart;
        const data = req.body;

        const upCart = await cartModel.findByIdAndUpdate(
            cartId,
            {
                $set: {
                    id_user: data.id_user,
                    id_product: data.id_product,
                    quantity: data.quantity,
                    price: data.price,
                    total: data.total,
                    status: data.status,
                    color: data.color,
                    size: data.size,
                    img_cart: data.img_cart
                }
            },
            { new: true }
        );


        if (upCart) {
            res.json({
                "status": 200,
                "message": "Cập nhật thành công",
                "data": upCart
            });
        } else {
            res.json({
                "status": 400,
                "message": "Không tìm thấy giỏ hàng để cập nhật",
                "data": []
            });
        }
    } catch (error) {
        console.error('Lỗi cập nhật giỏ hàng:', error);
        res.status(500).json({ error: 'Lỗi cập nhật giỏ hàng' });
    }
});


// xoá toàn bộ giỏ hàng http://localhost:3000/api/cart/user/:userId
router.delete('/del_cart/:idCart', async (req, res) => {
    try {
        const result = await cartModel.findByIdAndDelete(req.params.idCart);
        if (!result) return res.status(404).json({ message: "Không tìm thấy giỏ hàng" });
        res.status(200).json({ message: "Xóa thành công" });
    } catch (err) {
        res.status(500).json({ message: "Lỗi server" });
    }
});

// xóa tất cả giỏ hàng người dùng
router.delete('/delete_all_cart/:userId', async (req, res) => {
    try {
        const userId = req.params.userId;
        const objectId = new mongoose.Types.ObjectId(userId); // convert string sang ObjectId

        const result = await cartModel.deleteMany({ id_user: objectId });

        if (result.deletedCount > 0) {
            return res.status(200).json({ message: 'Đã xóa tất cả giỏ hàng thành công' });
        } else {
            return res.status(404).json({ message: 'Không tìm thấy giỏ hàng để xóa' });
        }
    } catch (error) {
        console.error("Lỗi xóa toàn bộ giỏ hàng:", error);
        return res.status(500).json({ message: 'Lỗi server' });
    }
});

// Xoá nhiều sản phẩm trong giỏ hàng (sau khi đặt hàng)
router.post('/delete_cart_items', async (req, res) => {
    try {
        const { cartIds } = req.body;  // [{...}, {...}] hoặc ["id1", "id2"]

        if (!Array.isArray(cartIds) || cartIds.length === 0) {
            return res.status(400).json({ message: 'Danh sách cartIds không hợp lệ' });
        }

        const result = await cartModel.deleteMany({ _id: { $in: cartIds } });

        res.status(200).json({
            message: 'Xóa các sản phẩm trong giỏ thành công',
            deletedCount: result.deletedCount
        });
    } catch (error) {
        console.error('Lỗi xóa nhiều sản phẩm trong giỏ:', error);
        res.status(500).json({ message: 'Lỗi server khi xóa giỏ hàng' });
    }
});

// category
// lấy ds product theo thể loại
router.get('/products_by_category/:categoryId', async (req, res) => {
    try {
        const categoryId = req.params.categoryId;
        const products = await productModel.find({ id_category: categoryId }).populate('id_category');
        if (!products || products.length === 0) {
            return res.status(404).json({ message: 'Không tìm thấy sản phẩm nào thuộc thể loại này' });
        }
        res.json(products);
    } catch (error) {
        console.error('Lỗi khi lấy sản phẩm theo thể loại:', error);
        res.status(500).json({ error: 'Lỗi server khi lấy sản phẩm theo thể loại' });
    }
});

//lấy ds category 
router.get('/list_category', async (req, res) => {
    await mongoose.connect(uri);
    let category = await categoryModel.find();
    res.send(category);
});


router.post('/add_category', upload.single('imgTL'), async (req, res) => {


    try {
        const titleTL = req.body.titleTL;
        const imgTL = req.file ? req.file.filename : null;
        console.log("🟢 File:", req.file);
        console.log("🟢 File name:", imgTL);
        const newTL = new categoryModel({
            title: titleTL,
            cateImg: imgTL
        });
        const kq = await newTL.save();
        console.log('Thêm thể loại thành công');
        let category = await categoryModel.find();
        res.send(category);
    } catch (error) {
        console.error('Thêm thể loại thất bại:', error);
        res.status(500).send('Lỗi server');
    }
})
// sua category
router.put('/edit_cate/:id', async (req, res) => {
    try {
        const id = req.params.id;
        const data = req.body;

        const kq = await categoryModel.findByIdAndUpdate(id, data, { new: true });

        if (kq) {
            console.log('Sửa thành công!');
            let cate = await categoryModel.find();
            res.send('Không tìm thấy thể loại để sửa!');

        }
    } catch (err) {
        res.send('Lỗi khi sửa')
    }
})
//xoa the loai
router.delete('/del_category/:id', async (req, res) => {
    const categoryId = req.params.id;

    try {
        // Kiểm tra xem có sản phẩm nào liên kết không
        const linkedProducts = await productModel.find({ id_category: categoryId });

        if (linkedProducts.length > 0) {
            return res.status(400).json({ message: 'Không thể xóa. Thể loại đang liên kết với sản phẩm.' });
        }

        // Nếu không liên kết thì xóa
        await categoryModel.findByIdAndDelete(categoryId);
        res.json({ message: 'Xóa thành công' });
    } catch (error) {
        console.error('Lỗi khi xóa thể loại:', error);
        res.status(500).json({ message: 'Lỗi server' });
    }
});


// lấy ds don hang 'http://localhost:3000/api/list_order'
router.get('/list_order', async (req, res) => {
    await mongoose.connect(uri);
    try {
        const orders = await orderModel.find()
            .sort({ date: -1 })
            .populate('id_user', 'name')
            .populate({
                path: 'products.id_product',
                select: 'nameproduct avt_imgproduct variations id_category',
                populate: {
                    path: 'id_category',
                    select: 'title' // Lấy đúng title của thể loại
                }
            });
        res.send(orders);
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Lỗi lấy danh sách đơn hàng' });
    }
});


router.get('/list_order/:userId', async (req, res) => {
    try {
        const orders = await orderModel.find({ id_user: req.params.userId })
            .populate('id_user')
            .populate({
                path: 'products.id_product',
                populate: {
                    path: 'id_category',
                    select: '_id name'  // chọn trường cần thiết để tránh quá nhiều dữ liệu
                }
            });

        const enrichedOrders = orders.map(order => {
            const totalQty = order.products.reduce((sum, item) => sum + item.quantity, 0);
            return {
                ...order.toObject(),
                quantity_order: totalQty
            };
        });

        res.json(enrichedOrders);
    } catch (err) {
        console.error('❌ Lỗi khi lấy đơn hàng theo user:', err);
        res.status(500).json({ message: 'Lỗi server khi lấy đơn hàng' });
    }
});



// thêm order 'http://localhost:3000/api/order'
// Thêm đơn hàng: http://localhost:3000/api/order/add_order
router.post('/add_order', async (req, res) => {
    try {
        const data = req.body;

        if (!Array.isArray(data.products)) {
            return res.status(400).json({ message: 'Dữ liệu products không hợp lệ' });
        }

        // Tính tổng số lượng sản phẩm trong đơn
        data.quantity_order = data.products.reduce((sum, item) => sum + item.quantity, 0);

        // Tạo đơn hàng mới
        const newOrder = await orderModel.create(data);
        if (!newOrder) {
            return res.status(500).json({ message: 'Không thể tạo đơn hàng' });
        }

        // 🔍 Truy vấn thông tin sản phẩm để tạo thông báo
        const productDetails = await Promise.all(data.products.map(async item => {
            try {
                const product = await productModel.findById(item.id_product);
                return {
                    id_product: item.id_product,
                    productName: product?.nameproduct || '',
                    img: product?.avt_imgproduct || ''
                };
            } catch (error) {
                console.error('❌ Không tìm thấy sản phẩm:', item.id_product);
                return {
                    id_product: item.id_product,
                    productName: '',
                    img: ''
                };
            }
        }));

        // 🔔 Tạo thông báo
        const newNotification = new notificationModel({
            userId: data.id_user,
            title: 'Đặt hàng thành công',
            content: `Đơn hàng <font color='#2196F3'> ${newOrder._id}</font> của bạn  đã được đặt thành công và đang được xử lý.`,
            type: 'order',
            isRead: false,
            createdAt: new Date(),
            orderId: newOrder._id,
            products: productDetails
        });

        await newNotification.save();

        // Populate lại đơn hàng để trả về chi tiết đầy đủ
        const populatedOrder = await orderModel.findById(newOrder._id)
            .populate('id_user')
            .populate({
                path: 'products.id_product',
                populate: {
                    path: 'id_category',
                    select: '_id name'
                }
            });

        console.log('✅ Thêm đơn hàng và tạo thông báo thành công:', newOrder._id);
        res.status(201).json(populatedOrder);
    } catch (error) {
        console.error('❌ Lỗi khi thêm đơn hàng:', error);
        res.status(500).json({ message: 'Lỗi server khi tạo đơn hàng' });
    }
});

// Xóa 1 thông báo theo ID
// DELETE http://localhost:3000/api/notification/:id
router.delete('/notification/:id', async (req, res) => {
    try {
        const id = req.params.id;
        const result = await notificationModel.findByIdAndDelete(id);
        if (!result) {
            return res.status(404).json({ message: 'Không tìm thấy thông báo để xóa' });
        }
        res.status(200).json({ message: 'Đã xóa thông báo thành công' });
    } catch (error) {
        console.error('❌ Lỗi khi xóa thông báo:', error);
        res.status(500).json({ message: 'Lỗi server khi xóa thông báo' });
    }
});





// huy don hang 'http://localhost:3000/api/order/ id'
router.delete('/del_order/:id', async (req, res) => {
    try {
        let id = req.params.id;
        const kq = await orderModel.deleteOne({ _id: id });
        if (kq) {
            console.log('Huy don hang thành công');
            let ord = await orderModel.find();
            res.send(ord);
        } else {
            res.send('Huy don hang không thành công');
        }
    } catch (error) {
        console.error('Lỗi khi xóa:', error);
        res.status(500).json({ error: 'Lỗi server khi xóa sản phẩm' });
    }
})

router.delete('/delete_all_orders', async (req, res) => {
    try {
        await orderModel.deleteMany({});
        res.status(200).json({ message: 'Đã xóa tất cả đơn hàng thành công' });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Lỗi khi xóa đơn hàng' });
    }
});
router.put('/cancel_order/:id', async (req, res) => {
    try {
        const id = req.params.id;
        const updatedOrder = await orderModel.findByIdAndUpdate(
            id,
            { status: 'Đã hủy' },
            { new: true }
        );

        if (!updatedOrder) {
            return res.status(404).json({ message: 'Không tìm thấy đơn hàng' });
        }

        res.status(200).json({
            message: 'Đã hủy đơn hàng thành công',
            order: updatedOrder
        });
    } catch (error) {
        console.error('❌ Lỗi khi hủy đơn hàng:', error);
        res.status(500).json({ message: 'Lỗi server khi hủy đơn hàng' });
    }
});


router.put('/updateOrderStatus/:orderId', async (req, res) => {
    try {
        const orderId = req.params.orderId;
        const { status, cancelReason } = req.body;

        if (!status) {
            return res.status(400).json({ message: 'Trạng thái không được để trống' });
        }

        const updateData = { status };
        if (cancelReason) {
            updateData.cancelReason = cancelReason;
        }

        // Cập nhật trạng thái đơn hàng
        await orderModel.findByIdAndUpdate(
            orderId,
            updateData,
            { new: true, runValidators: true }
        );

        // 🔁 Lấy lại đơn hàng đã cập nhật và populate
        const order = await orderModel.findById(orderId)
            .populate('id_user')
            .populate('products.id_product'); // populate sản phẩm trong đơn hàng

        if (!order) {
            return res.status(404).json({ message: 'Không tìm thấy đơn hàng' });
        }

        // 🔔 Nếu trạng thái là "Đang giao", tạo thông báo giao hàng thành công
        if (status === 'Đang giao') {
            const newNotification = new notificationModel({
                userId: order.id_user._id,
                title: 'Giao hàng thành công',
                content: `Đơn hàng <font color='#2196F3'>${order._id}</font> của bạn đã được giao thành công. Cảm ơn bạn đã mua sắm tại ShopBePoly!`,
                type: 'delivery',
                isRead: false,
                createdAt: new Date(),
                orderId: order._id,
                products: order.products.map(item => ({
                    id_product: item.id_product?._id,
                    productName: item.id_product?.nameproduct || '',
                    img: item.id_product?.avt_imgproduct || ''
                }))
            });

            await newNotification.save();
        }

        return res.status(200).json({ message: 'Cập nhật trạng thái thành công', order });
    } catch (error) {
        console.error(error);
        return res.status(500).json({ message: 'Lỗi máy chủ nội bộ', error: error.message });
    }
});




//Comment
// ds comment 'http://localhost:3000/api/list_comment'
router.get('/api/list_comment/:userId', async (req, res) => {
    try {
        const cartItems = await cartModel.find({ id_user: req.params.userId });
        res.json(cartItems);
    } catch (error) {
        console.error('Lỗi', error);
        res.status(500).json({ error: 'Lỗi' });
    }
});

// thêm comment 'http://localhost:3000/api/add_comment'
router.post('/add_comment', async (req, res) => {

    let data = req.body;
    let kq = await commentModel.create(data);

    if (kq) {
        console.log('Thêm comment thành công');
        let comment = await commentModel.find();
        res.send(comment);
    } else {
        console.log('Thêm comment không thành công');
    }

})

// sửa comment 'http://localhost:3000/api/up_comment/ id'
router.put('/up_comment/:id', async (req, res) => {
    try {
        const id = req.params.id;
        const data = req.body;

        const kq = await commentModel.findByIdAndUpdate(id, data, { new: true });

        if (kq) {
            console.log('Sửa thành công');
            let usr = await commentModel.find();
            res.send(usr);
        } else {
            res.send('Không tìm thấy comment để sửa');
        }
    } catch (error) {
        res.send('Lỗi khi sửa')
    }
})



router.get('/messages', async (req, res) => {
    try {
        const { from, to } = req.query;

        if (!from || !to) {
            return res.status(400).json({ message: 'Thiếu from hoặc to trong query' });
        }

        const messages = await messageModel.find({
            $or: [
                { from, to },
                { from: to, to: from }
            ]
        }).sort({ timestamp: 1 });

        res.json(messages);
    } catch (err) {
        console.error('Lỗi lấy tin nhắn:', err);
        res.status(500).json({ message: 'Lỗi server khi lấy tin nhắn' });
    }
});


router.post('/messages', async (req, res) => {
    const { from, to, content } = req.body;

    if (!from || !to || !content) {
        return res.status(400).json({ message: 'Thiếu from, to hoặc content trong body' });
    }

    try {

        const newMessage = new messageModel({ from, to, content, timestamp: new Date() });
        await newMessage.save();


        const hasAdminReplied = await messageModel.exists({
            from: to,
            to: from
        });

        if (!hasAdminReplied) {
            const autoReply = new messageModel({
                from: to,
                to: from,
                content: "Chào bạn! Bạn cần giúp đỡ gì không? ",
                timestamp: new Date()
            });
            await autoReply.save();
        }

        res.status(201).json({ message: 'Gửi tin nhắn thành công', data: newMessage });
    } catch (err) {
        console.error('Lỗi khi gửi tin nhắn:', err);
        res.status(500).json({ message: 'Lỗi server khi gửi tin nhắn' });
    }
});

// Đổi mật khẩu user
router.put('/up_password/:id', async (req, res) => {
    try {
        const id = req.params.id;
        const { oldPassword, newPassword } = req.body;

        // Tìm user theo id
        const user = await userModel.findById(id);
        if (!user) {
            return res.status(404).json({ message: 'Không tìm thấy người dùng' });
        }

        // Kiểm tra mật khẩu cũ
        if (user.password !== oldPassword) {
            return res.status(400).json({ message: 'Mật khẩu cũ không đúng' });
        }

        // Cập nhật mật khẩu mới
        user.password = newPassword;
        await user.save();

        res.json({ message: 'Đổi mật khẩu thành công' });
    } catch (error) {
        console.error('Lỗi đổi mật khẩu:', error);
        res.status(500).json({ message: 'Lỗi server khi đổi mật khẩu' });
    }
});
// POST /auth/reset-password-by-email
router.post('/auth/reset-password-by-email', async (req, res) => {
    const { email, newPassword } = req.body;

    try {
        const user = await userModel.findOne({ email });
        if (!user) {
            return res.status(404).json({ message: 'Không tìm thấy người dùng' });
        }

        user.password = newPassword;
        await user.save();

        res.json({ message: 'Đặt lại mật khẩu thành công' });
    } catch (error) {
        console.error('Lỗi đặt lại mật khẩu:', error);
        res.status(500).json({ message: 'Lỗi server' });
    }
});

router.post("/add_favorite", async (req, res) => {
    try {
        const { id_user, id_product } = req.body;

        const exists = await favoriteModel.findOne({ id_user, id_product });
        if (exists) {
            return res.status(400).json({ message: "Đã tồn tại trong yêu thích" });
        }

        const product = await productModel.findById(id_product);
        const user = await userModel.findById(id_user);
        if (!product || !user) {
            return res.status(404).json({ message: "Không tìm thấy user hoặc product" });
        }

        const favorite = new favoriteModel({ id_user, id_product });
        await favorite.save();
        res.status(200).json(favorite);
    } catch (err) {
        console.error("Add favorite error:", err);
        res.status(500).json({ message: "Thêm yêu thích thất bại", error: err });
    }
});



router.delete('/remove_favorite', async (req, res) => {
    const { id_user, id_product } = req.query;
    try {
        const result = await Favorite.deleteOne({ id_user, id_product });
        if (result.deletedCount === 0) {
            return res.status(404).json({ message: 'Không tìm thấy yêu thích để xoá' });
        }
        res.json({ message: 'Đã xoá yêu thích thành công' });
    } catch (err) {
        res.status(500).json({ message: 'Lỗi server khi xoá yêu thích' });
    }
});



router.get('/favorites/:userId', async (req, res) => {
    try {
        const favorites = await Favorite.find({ id_user: req.params.userId })
            .populate('id_product');
        res.status(200).json(favorites);
    } catch (error) {
        console.error('Lỗi khi lấy danh sách yêu thích:', error);
        res.status(500).json({
            message: 'Lỗi khi lấy danh sách yêu thích',
            error: error.message || error
        });
    }
});
router.get('/get-admin', async (req, res) => {
    try {
        const admin = await userModel.findOne({ role: 2 }); // hoặc 2 nếu bạn dùng 2 là admin
        if (!admin) {
            return res.status(404).json({ message: 'Admin not found' });
        }
        res.json(admin);
    } catch (error) {
        console.error('Lỗi khi lấy admin:', error);
        res.status(500).json({ message: 'Server error' });
    }
});

// Gửi mã xác minh
router.post('/send-verification-code', async (req, res) => {
    const { email } = req.body;

    const code = Math.floor(100000 + Math.random() * 900000).toString();

    try {
        await VerifyCode.create({ email: email.trim().toLowerCase(), code });

        await sendEmail(email, 'Mã xác nhận', `Mã xác nhận của bạn là: ${code}`);

        return res.sendStatus(200);
    } catch (err) {
        console.error('Lỗi gửi mã xác minh:', err);
        return res.status(500).json({ message: 'Lỗi máy chủ' });
    }
});

// Xác minh mã
router.post('/verify-code', async (req, res) => {
    const { email, code } = req.body;

    try {
        const record = await VerifyCode.findOne({
            email: email.trim().toLowerCase(),
            code: code.trim()
        });

        if (!record) {
            return res.status(400).json({ message: 'Không tìm thấy mã xác minh' });
        }

        await VerifyCode.deleteOne({ _id: record._id });

        return res.status(200).json({ message: 'Xác minh thành công' });
    } catch (err) {
        console.error('Lỗi xác minh mã:', err);
        return res.status(500).json({ message: 'Lỗi máy chủ' });
    }
});

// Thống kê doanh thu theo ngày / tháng / năm 
router.get('/statistics', async (req, res) => {
    try {
        const { type, year, month, day } = req.query;

        const matchStage = {
            status: { $ne: 'Đã hủy' } 
        };

        const dateFilter = {};

        if (type === 'day' && year && month && day) {
            const paddedMonth = month.padStart(2, '0');
            const paddedDay = day.padStart(2, '0');
            const start = new Date(`${year}-${paddedMonth}-${paddedDay}T00:00:00+07:00`);
            const end = new Date(start);
            end.setDate(start.getDate() + 1);
            dateFilter.$gte = start;
            dateFilter.$lt = end;

        } else if (type === 'month' && year && month) {
            const paddedMonth = month.padStart(2, '0');
            const start = new Date(`${year}-${paddedMonth}-01T00:00:00+07:00`);
            const nextMonth = new Date(start);
            nextMonth.setMonth(start.getMonth() + 1);
            dateFilter.$gte = start;
            dateFilter.$lt = nextMonth;

        } else if (type === 'year' && year) {
            const start = new Date(`${year}-01-01T00:00:00+07:00`);
            const end = new Date(`${+year + 1}-01-01T00:00:00+07:00`);
            dateFilter.$gte = start;
            dateFilter.$lt = end;
        }

        if (Object.keys(dateFilter).length > 0) {
            matchStage.date = dateFilter;
        }

        const result = await orderModel.aggregate([
            { $match: matchStage },
            {
                $group: {
                    _id: null,
                    totalRevenue: {
                        $sum: {
                            $toDouble: "$total"
                        }
                    },
                    totalOrders: { $sum: 1 }
                }
            }
        ]);

        if (result.length === 0) {
            return res.json({
                totalRevenue: 0,
                totalOrders: 0
            });
        }

        const { totalRevenue, totalOrders } = result[0];
        res.json({ totalRevenue, totalOrders });

    } catch (error) {
        console.error('Lỗi thống kê doanh thu:', error);
        res.status(500).json({ message: 'Lỗi server khi thống kê doanh thu' });
    }
});


// Top 5 sản phẩm bán chạy
router.get('/top-products', async (req, res) => {
  try {
    const topProducts = await orderModel.aggregate([
      { $match: { status: { $ne: "Đã hủy" } } }, // Bỏ đơn đã hủy
      { $unwind: "$products" },
      {
        $group: {
          _id: "$products.id_product",
          totalQuantity: { $sum: "$products.quantity" }
        }
      },
      {
        $lookup: {
          from: "products",
          localField: "_id",
          foreignField: "_id",
          as: "productInfo"
        }
      },
      { $unwind: "$productInfo" },
      {
        $project: {
          _id: 0,
          id: "$_id",
          name: "$productInfo.nameproduct",
          image: "$productInfo.avt_imgproduct",
          totalQuantity: 1
        }
      },
      { $sort: { totalQuantity: -1 } },
      { $limit: 5 }
    ]);

    res.json(topProducts);
  } catch (error) {
    console.error("Lỗi khi thống kê top sản phẩm:", error);
    res.status(500).json({ message: "Lỗi server", error });
  }
});

router.get('/statistics/overview', async (req, res) => {
  try {
    const { start, end } = req.query;

    let startDate = start ? new Date(start + "T00:00:00+07:00") : null;
    let endDate = end ? new Date(end + "T23:59:59.999+07:00") : null;

    const dateFilter = {};
    if (startDate) dateFilter.$gte = startDate;
    if (endDate) dateFilter.$lte = endDate;

    const orderFilter = Object.keys(dateFilter).length ? { date: dateFilter } : {};

    // Đếm tổng sản phẩm, người dùng (không lọc theo ngày vì user không có trường createdAt)
    const totalProducts = await productModel.countDocuments();
    const totalUsers = await userModel.countDocuments();

    // Đếm đơn hàng theo khoảng thời gian lọc
    const totalOrders = await orderModel.countDocuments(orderFilter);

    // Lấy tất cả đơn hàng theo filter để tính tổng doanh thu và trạng thái
    const allOrders = await orderModel.find(orderFilter);

    let totalRevenue = 0;
    let countDelivered = 0;
    let countProcessing = 0;
    let countCancelled = 0;

    allOrders.forEach(order => {
      const status = order.status || '';
      const orderTotal = Number(order.total) || 0;

      if (status !== 'Đã hủy') {
        totalRevenue += orderTotal;
      }

      if (status === 'Đã giao') countDelivered++;
      else if (status === 'Đang xử lý') countProcessing++;
      else if (status === 'Đã hủy') countCancelled++;
    });

    res.json({
      totalProducts,
      totalUsers,
      totalOrders,
      totalRevenue,
      countDelivered,
      countProcessing,
      countCancelled
    });
  } catch (error) {
    console.error('Lỗi thống kê tổng quan:', error);
    res.status(500).json({ message: 'Lỗi server khi lấy thống kê tổng quan' });
  }
});



app.use(express.json());