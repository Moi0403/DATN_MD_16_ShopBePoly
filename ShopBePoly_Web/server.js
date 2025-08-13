const express = require('express');
const mongoose = require('mongoose');
const router = express.Router();
const app = express();
const cors = require('cors');
const port = 3000;
const http = require('http');
const server = http.createServer(app);

const bodyParser = require('body-parser');
app.use(bodyParser.json());
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use('/uploads', express.static('uploads', {
    setHeaders: res => res.set('Cache-Control', 'no-store')
}));
const fs = require('fs').promises;

const multer = require('multer');
const path = require('path');

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
const VerifyCode = require('./Database/verifyCode');
const { userSockets, initWebSocket } = require('./serverWS');
const Banner = require('./Database/bannerModel');

const uri = COMOMJS.uri;

// Kết nối MongoDB
mongoose.connect(uri, {
    useNewUrlParser: true,
    useUnifiedTopology: true,
    serverSelectionTimeoutMS: 5000
})
    .then(() => console.log('Connected to MongoDB'))
    .catch((err) => console.error('MongoDB connection error:', err));

initWebSocket(server);
    
// Khởi động server
server.listen(port, () => {
    console.log(`Server is running on port ${port}`);
});

const storageBanner = multer.diskStorage({
    destination: async (req, file, cb) => {
        const uploadDir = await ensureUploadsDir();
        cb(null, uploadDir);
    },
    filename: (req, file, cb) => {
        const ext = path.extname(file.originalname).toLowerCase() || '.' + file.mimetype.split('/')[1];
        const uniqueName = `banner-${Date.now()}-${Math.round(Math.random() * 1e9)}${ext}`;
        cb(null, uniqueName);
    }
});

const uploadBanner = multer({
    storage: storageBanner,
    fileFilter: (req, file, cb) => {
        const allowedTypes = ['image/jpeg', 'image/png', 'image/gif'];
        if (!allowedTypes.includes(file.mimetype)) {
            return cb(new Error('Chỉ chấp nhận file ảnh JPEG, PNG, GIF!'));
        }
        cb(null, true);
    },
    limits: { fileSize: 5 * 1024 * 1024 }
});

const ensureUploadsDir = async () => {
    const baseDir = path.dirname(require.main.filename);
    const dir = path.join(baseDir, 'uploads');
    try {
        await fs.access(dir);
    } catch {
        await fs.mkdir(dir, { recursive: true });
        console.log('Đã tạo thư mục uploads tại:', dir);
    }
    return dir;
};

const storageAvatar = multer.diskStorage({
    destination: async (req, file, cb) => {
        const uploadDir = await ensureUploadsDir();
        cb(null, uploadDir);
    },
    filename: async (req, file, cb) => {
        const ext = path.extname(file.originalname).toLowerCase();
        const userId = req.params.id;
        const fileName = `${userId}${ext}`;

        // Xóa ảnh cũ nếu tồn tại
        const user = await userModel.findById(userId);
        if (user?.avt_user && user.avt_user !== fileName) {
            const oldPath = path.join(await ensureUploadsDir(), user.avt_user);
            if (await fs.access(oldPath).then(() => true).catch(() => false)) {
                await fs.unlink(oldPath);
            }
        }

        cb(null, fileName);
    }
});

const storageProduct = multer.diskStorage({
    destination: async (req, file, cb) => {
        const uploadDir = await ensureUploadsDir();
        cb(null, uploadDir);
    },
    filename: (req, file, cb) => {
        const ext = path.extname(file.originalname).toLowerCase() || '.' + file.mimetype.split('/')[1];
        const uniqueName = `${Date.now()}-${Math.round(Math.random() * 1e9)}${ext}`;
        cb(null, uniqueName);
    }
});

const uploadAvatar = multer({ storage: storageAvatar });
const uploadProduct = multer({ storage: storageProduct });

const storageCategory = multer.diskStorage({
    destination: async (req, file, cb) => {
        const uploadDir = await ensureUploadsDir();
        cb(null, uploadDir);
    },
    filename: (req, file, cb) => {
        const ext = path.extname(file.originalname).toLowerCase() || '.' + file.mimetype.split('/')[1];
        const uniqueName = `${Date.now()}-${Math.round(Math.random() * 1e9)}${ext}`;
        console.log('Tạo tên file category:', uniqueName, 'mimetype:', file.mimetype);
        cb(null, uniqueName);
    }
});

const uploadCategory = multer({
    storage: storageCategory,
    fileFilter: (req, file, cb) => {
        const allowedTypes = ['image/jpeg', 'image/png', 'image/gif'];
        console.log('Kiểm tra mimetype category:', file.mimetype, 'originalname:', file.originalname);
        if (!allowedTypes.includes(file.mimetype)) {
            return cb(new Error('Chỉ chấp nhận file ảnh JPEG, PNG, GIF!'));
        }
        cb(null, true);
    },
    limits: { fileSize: 5 * 1024 * 1024 }
});

router.use((err, req, res, next) => {
    console.error('Middleware lỗi:', err);
    res.status(500).json({ error: 'Lỗi server: ' + err.message });
});

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

router.post('/upload-avatar/:id', uploadAvatar.single('avt_user'), async (req, res) => {
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
        res.json(products);
    } catch (err) {
        console.error('Error fetching products:', err);
        res.status(500).json({ error: 'Lỗi khi lấy danh sách sản phẩm' });
    }
});

app.use('/api/users', router);
app.use('/api', router);



router.get('/list_product', async (req, res) => {
    try {
        const products = await productModel.find().populate('id_category');
        res.json(products);
    } catch (error) {
        console.error('Lỗi khi lấy danh sách sản phẩm:', error);
        res.status(500).send('Lỗi server khi lấy sản phẩm');
    }
});

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

        // 🪵 Log file để debug nếu cần
        console.log("📦 FILES UPLOADED:");
        files.forEach(f => {
            console.log(` - field: ${f.fieldname}, name: ${f.filename}`);
        });

        // ✅ Gán ảnh cho từng variation (cải tiến: fallback tốt hơn)
        // variations.forEach((variation, index) => {
        //     const colorCode = variation.color?.code?.toLowerCase() || '';
        //     const size = variation.size;

        //     const fieldByKey = `variation-${colorCode}-${size}`;
        //     let matchedFiles = files.filter(f => f.fieldname === fieldByKey);

        //     // Fallback theo index: variationImages-0, variationImages-1
        //     if (matchedFiles.length === 0) {
        //         const fieldByIndex = `variationImages-${index}`;
        //         matchedFiles = files.filter(f => f.fieldname === fieldByIndex);
        //     }

        //     // Fallback cuối: tìm ảnh có tên chứa màu hoặc size
        //     if (matchedFiles.length === 0) {
        //         matchedFiles = files.filter(f =>
        //             f.originalname?.toLowerCase().includes(colorCode) ||
        //             f.originalname?.includes(size?.toString())
        //         );
        //     }

        //     // ✅ Luôn gán ảnh nếu tìm được
        //     variation.list_imgproduct = matchedFiles.map(f => f.filename);
        //     variation.image = matchedFiles[0]?.filename || '';
        // });
        variations.forEach((variation, vIndex) => {
            const colorIndex = vIndex; // fallback theo index gửi từ client
            const matchedFiles = [];

            // Quét tất cả file có fieldname dạng 'variationImages-<colorIndex>-<subIndex>'
            files.forEach(file => {
                const regex = new RegExp(`^variationImages-${colorIndex}-\\d+$`);
                if (regex.test(file.fieldname)) {
                    matchedFiles.push(file);
                }
            });

            // Nếu không có ảnh theo index thì fallback tìm theo màu (color code)
            if (matchedFiles.length === 0 && variation.color?.code) {
                const colorCode = variation.color.code.replace("#", "").toLowerCase();
                files.forEach(file => {
                    if (file.originalname?.toLowerCase().includes(colorCode)) {
                        matchedFiles.push(file);
                    }
                });
            }

            // Gán ảnh
            variation.list_imgproduct = matchedFiles.map(f => f.filename);
            variation.image = matchedFiles[0]?.filename || '';
        });

        // ✅ Gộp toàn bộ ảnh lại cho list_imgproduct chính
        const mergedImages = [];

        variations.forEach(variation => {
            if (Array.isArray(variation.list_imgproduct)) {
                variation.list_imgproduct.forEach(img => {
                    if (img && !mergedImages.includes(img)) {
                        mergedImages.push(img);
                    }
                });
            }
        });

        const additionalImgs = files.filter(f => f.fieldname === 'list_imgproduct');
        additionalImgs.forEach(f => {
            if (!mergedImages.includes(f.filename)) {
                mergedImages.push(f.filename);
            }
        });

        // ✅ Tạo sản phẩm mới
        const newProduct = new productModel({
            nameproduct: body.name_pro,
            id_category: body.category_pro,
            price_enter: body.price_enter,
            price: body.price_pro,
            description: body.mota_pro,
            avt_imgproduct: avt_imgpro?.filename || '',
            list_imgproduct: mergedImages,
            discount: body.discount || "",
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



router.put('/update_product/:id', uploadProduct.any(), async (req, res) => {
    try {
        const { id } = req.params;
        if (!mongoose.Types.ObjectId.isValid(id)) {
            return res.status(400).json({ error: 'ID sản phẩm không hợp lệ' });
        }

        const { name_pro, category_pro, price_pro, price_enter, mota_pro, discount, variations } = req.body;
        const parsedVariations = JSON.parse(variations || '[]');

        const existingProduct = await productModel.findById(id);
        if (!existingProduct) {
            return res.status(404).json({ error: 'Sản phẩm không tồn tại' });
        }

        const avt_imgproduct = req.files.find(file => file.fieldname === 'avt_imgpro')?.filename || existingProduct.avt_imgproduct;

        const variationImages = {};
        req.files.forEach(file => {
            if (file.fieldname.startsWith('variationImages-')) {
                const [_, colorIndex, fileIndex] = file.fieldname.match(/variationImages-(\d+)-(\d+)/) || [];
                if (colorIndex && fileIndex) {
                    if (!variationImages[colorIndex]) {
                        variationImages[colorIndex] = [];
                    }
                    variationImages[colorIndex].push(file.filename);
                }
            }
        });

        console.log('req.files:', req.files);
        console.log('variationImages:', variationImages);
        console.log('parsedVariations:', parsedVariations);

        const updatedVariations = parsedVariations.map((variation, index) => {
            const images = variationImages[index] || variation.list_imgproduct || [];
            const existingVariation = existingProduct.variations.find(
                v => v.size === variation.size && v.color.name === variation.color.name && v.color.code === variation.color.code
            );

            return {
                _id: existingVariation ? existingVariation._id : new mongoose.Types.ObjectId(),
                size: variation.size,
                stock: variation.stock,
                sold: variation.sold || (existingVariation ? existingVariation.sold : 0),
                color: variation.color,
                image: images[0] || (existingVariation ? existingVariation.image : ''),
                list_imgproduct: images.length > 0 ? images : (existingVariation ? existingVariation.list_imgproduct : []),
            };
        });

        const list_imgproduct = updatedVariations
            .flatMap(variation => variation.list_imgproduct)
            .filter((img, index, self) => img && self.indexOf(img) === index);

        const updatedProduct = await productModel.findByIdAndUpdate(
            id,
            {
                nameproduct: name_pro || existingProduct.nameproduct,
                id_category: category_pro || existingProduct.id_category,
                price: price_pro ? Number(price_pro) : existingProduct.price,
                price_enter: price_enter ? Number(price_enter) : existingProduct.price_enter,
                description: mota_pro || existingProduct.description,
                discount: discount !== undefined ? Number(discount) : existingProduct.discount,
                avt_imgproduct,
                list_imgproduct: list_imgproduct.length > 0 ? list_imgproduct : existingProduct.list_imgproduct,
                variations: updatedVariations,
            },
            { new: true }
        );

        res.status(200).json(updatedProduct);
    } catch (error) {
        console.error('Lỗi khi cập nhật sản phẩm:', error);
        res.status(500).json({ error: 'Lỗi khi cập nhật sản phẩm: ' + error.message });
    }
});

router.put('/update_stock', async (req, res) => {
    const { productId, color, size, stock } = req.body;
    try {
        const product = await productModel.findById(productId);
        if (!product) return res.status(404).json({ message: 'Không tìm thấy sản phẩm' });

        let updated = false;
        product.variations.forEach(variation => {
            console.log(`👉 So sánh: [${variation.color?.name}] === [${color}], [${variation.size}] === [${size}]`);

            if (
                variation.color?.name?.toString().trim().toLowerCase() === color?.toString().trim().toLowerCase() &&
                variation.size?.toString().trim() === size?.toString().trim()
            ) {
                variation.stock = stock;
                updated = true;
            }
        });

        if (!updated) return res.status(404).json({ message: 'Không tìm thấy biến thể' });

        await product.save();
        res.json({ message: 'Cập nhật thành công' });
    } catch (err) {
        res.status(500).json({ message: 'Lỗi server', error: err.message });
    }
});

router.put('/updateStockSold', async (req, res) => {
    try {
        const { productId, color, size, quantity } = req.body;

        const product = await productModel.findById(productId);
        if (!product) return res.status(404).json({ message: 'Không tìm thấy sản phẩm' });

        // ✅ Tìm đúng biến thể theo màu và size
        const variation = product.variations.find(v =>
            v.color.name.toLowerCase() === color.toLowerCase() && v.size == size
        );

        if (!variation) return res.status(404).json({ message: 'Không tìm thấy biến thể với màu và size tương ứng' });

        // ✅ Cập nhật stock và sold
        variation.stock = Math.max(0, variation.stock - quantity);
        variation.sold = (variation.sold || 0) + quantity;

        await product.save();
        res.json({ message: 'Cập nhật stock & sold thành công' });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Lỗi server', error: err.message });
    }
});


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
});

router.get('/search_product', async (req, res) => {
    try {
        const keyword = req.query.q;
        if (!keyword || keyword.trim() === "") {
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

// User routes...
router.get('/list_user', async (req, res) => {
    try {
        const users = await userModel.find().select('-password');
        res.json(users);
    } catch (error) {
        console.error('Lỗi khi lấy danh sách user:', error);
        res.status(500).json({ error: 'Lỗi server' });
    }
});

router.post('/add_user', uploadAvatar.fields([{ name: 'avt_user', maxCount: 1 }]), async (req, res) => {
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
});

router.put('/up_user/:id', uploadAvatar.single('avt_user'), async (req, res) => {
    try {
        const id = req.params.id;
        const data = req.body;
        console.log('Dữ liệu nhận được từ app:', data);

        if (data.birthday_user) {
            data.birthday = data.birthday_user;
            delete data.birthday_user;
        }
        if (data.gender_user) {
            data.gender = data.gender_user;
            delete data.gender_user;
        }
        if (req.file) {
            data.avt_user = req.file.filename; // Sử dụng filename thay vì path
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
        res.send('Lỗi khi sửa');
    }
});

router.put('/up_user/:id', uploadAvatar.single('avatar'), async (req, res) => {
    try {
        const id = req.params.id;
        const data = req.body;
        if (req.file) {
            data.avatar = req.file.filename;
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
});

router.post('/register', async (req, res) => {
    let { username, password, name, email, phone_number, birthday, gender } = req.body;
    phone_number = Number(phone_number);

    try {
        const existingUser = await userModel.findOne({ username });
        if (existingUser) {
            return res.status(400).json({ message: 'Tên người dùng đã tồn tại' });
        }

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

        user.isOnline = true;
        await user.save();
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

router.get('/users_online', async (req, res) => {
    try {
        const onlineUsers = await userModel.countDocuments({ isOnline: true });
        res.json({ online: onlineUsers });
    } catch (err) {
        console.error('Error fetching online users:', err);
        res.status(500).json({ error: 'Server error' });
    }
});

router.get('/list_cart/:userId', async (req, res) => {
    try {
        const cartItems = await cartModel.find({ id_user: req.params.userId })
            .populate({
                path: 'id_product',
                populate: { path: 'id_category' }
            });
        res.json(cartItems);
    } catch (error) {
        console.error('Lỗi khi lấy giỏ hàng:', error);
        res.status(500).json({ error: 'Lỗi khi lấy giỏ hàng' });
    }
});

router.post('/add_cart', async (req, res) => {
    try {
        const { id_user, id_product, img_cart, quantity, price, size, color, status } = req.body;
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

router.delete('/del_cart/:idCart', async (req, res) => {
    try {
        const result = await cartModel.findByIdAndDelete(req.params.idCart);
        if (!result) return res.status(404).json({ message: "Không tìm thấy giỏ hàng" });
        res.status(200).json({ message: "Xóa thành công" });
    } catch (err) {
        res.status(500).json({ message: "Lỗi server" });
    }
});

router.delete('/delete_all_cart/:userId', async (req, res) => {
    try {
        const userId = req.params.userId;
        const objectId = new mongoose.Types.ObjectId(userId);

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

router.post('/delete_cart_items', async (req, res) => {
    try {
        const { cartIds } = req.body;

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

router.get('/list_category', async (req, res) => {
    await mongoose.connect(uri);
    let category = await categoryModel.find();
    res.send(category);
});

router.post('/add_category', uploadCategory.single('imgTL'), async (req, res) => {
    try {
        const titleTL = req.body.titleTL;
        const imgTL = req.file ? req.file.filename : null;

        console.log('🟢 File:', req.file);
        console.log('🟢 File name:', imgTL);
        console.log('🟢 Body:', req.body);
        console.log('🟢 Destination:', req.file?.destination);

        if (!titleTL) {
            return res.status(400).json({ error: 'Tiêu đề thể loại không được để trống' });
        }

        const newTL = new categoryModel({
            title: titleTL,
            cateImg: imgTL,
        });

        const kq = await newTL.save();
        console.log('Thêm thể loại thành công:', kq);

        let category = await categoryModel.find();
        console.log('Danh sách thể loại sau khi thêm:', category);

        res.status(201).json(category);
    } catch (error) {
        console.error('Thêm thể loại thất bại:', error);
        if (req.file) {
            await fs.unlink(req.file.path).catch(err => console.error('Lỗi xóa file:', err));
        }
        res.status(500).json({ error: 'Lỗi server: ' + error.message });
    }
});

router.put('/edit_cate/:id', async (req, res) => {
    try {
        const id = req.params.id;
        const data = req.body;

        const kq = await categoryModel.findByIdAndUpdate(id, data, { new: true });

        if (kq) {
            console.log('Sửa thành công!');
            let cate = await categoryModel.find();
            res.send(cate); // Sửa từ 'Không tìm thấy thể loại để sửa!' thành trả về danh sách
        } else {
            res.send('Không tìm thấy thể loại để sửa!');
        }
    } catch (err) {
        res.send('Lỗi khi sửa');
    }
});

router.delete('/del_category/:id', async (req, res) => {
    const categoryId = req.params.id;

    try {
        const linkedProducts = await productModel.find({ id_category: categoryId });

        if (linkedProducts.length > 0) {
            return res.status(400).json({ message: 'Không thể xóa. Thể loại đang liên kết với sản phẩm.' });
        }

        await categoryModel.findByIdAndDelete(categoryId);
        res.json({ message: 'Xóa thành công' });
    } catch (error) {
        console.error('Lỗi khi xóa thể loại:', error);
        res.status(500).json({ message: 'Lỗi server' });
    }
});

router.get('/list_order', async (req, res) => {
    await mongoose.connect(uri);
    try {
        const orders = await orderModel.find()
            .sort({ date: -1 })
            .populate('id_user', 'name phone_number')
            .populate({
                path: 'products.id_product',
                select: 'nameproduct avt_imgproduct variations id_category',
                populate: {
                    path: 'id_category',
                    select: 'title'
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
                    select: '_id name'
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

router.post('/add_order', async (req, res) => {
    try {
        const data = req.body;

        if (!Array.isArray(data.products)) {
            return res.status(400).json({ message: 'Dữ liệu products không hợp lệ' });
        }

        // Tính tổng số lượng sản phẩm
        data.quantity_order = data.products.reduce((sum, item) => sum + item.quantity, 0);

        // ⚠️ Kiểm tra nếu không có orderCode thì tự tạo để đảm bảo không null
        if (!data.id_order || data.id_order.trim() === '') {
            const generateIdOrder = () => {
                const datePart = new Date().toISOString().slice(2, 10).replace(/-/g, '');
                const randomPart = Math.random().toString(36).substring(2, 6).toUpperCase();
                return `ORD${datePart}${randomPart}`;
            };
            data.id_order = generateIdOrder();
        }

        // ⚠️ Giờ dữ liệu đã có orderCode, tạo đơn
        const newOrder = await orderModel.create(data);
        if (!newOrder) {
            return res.status(500).json({ message: 'Không thể tạo đơn hàng' });
        }

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

        const newNotification = new notificationModel({
            userId: data.id_user,
            title: 'Đặt hàng thành công',
            content: `Đơn hàng <font color='#2196F3'>${data.id_order}</font> của bạn đã được đặt thành công và đang được xử lý.`,
            type: 'order',
            isRead: false,
            createdAt: new Date(),
            orderId: newOrder._id,
            products: productDetails
        });

        await newNotification.save();

        const populatedOrder = await orderModel.findById(newOrder._id)
            .populate('id_user')
            .populate({
                path: 'products.id_product',
                populate: {
                    path: 'id_category',
                    select: '_id name'
                }
            });

        console.log('✅ Đơn hàng đã tạo:', data.id_order);
        res.status(201).json(populatedOrder);
    } catch (error) {
        console.error('❌ Lỗi khi thêm đơn hàng:', error);
        res.status(500).json({ message: 'Lỗi server khi tạo đơn hàng' });
    }
});


// API lấy đơn hàng theo ID (dùng khi click vào thông báo)
router.get('/order/:orderId', async (req, res) => {
    try {
        const orderId = req.params.orderId;

        const order = await orderModel.findById(orderId)
            .populate('id_user', 'name phone_number')
            .populate({
                path: 'products.id_product',
                select: 'nameproduct avt_imgproduct variations id_category',
                populate: {
                    path: 'id_category',
                    select: 'title'
                }
            });

        if (!order) {
            return res.status(404).json({ message: 'Không tìm thấy đơn hàng' });
        }

        // Tính tổng số lượng
        const totalQty = order.products.reduce((sum, item) => sum + item.quantity, 0);

        res.json({
            ...order.toObject(),
            quantity_order: totalQty
        });
    } catch (err) {
        console.error('❌ Lỗi khi lấy chi tiết đơn hàng:', err);
        res.status(500).json({ message: 'Lỗi server khi lấy chi tiết đơn hàng' });
    }
});

router.get('/search_order', async (req, res) => {
    try {
        const { code } = req.query;
        
        if (!code) {
            return res.status(400).json({ error: 'Code parameter is required' });
        }

        console.log('Searching for order with code:', code);
        console.log('Is valid ObjectId:', mongoose.Types.ObjectId.isValid(code));

        let orders = [];

        // Kiểm tra xem code có phải là ObjectId hợp lệ không
        if (mongoose.Types.ObjectId.isValid(code)) {
            console.log('Searching by ObjectId...');
            // Tìm đơn hàng theo ObjectId chính xác
            const order = await orderModel.findById(code)
                .populate('id_user', 'name phone_number')
                .populate({
                    path: 'products.id_product',
                    select: 'nameproduct avt_imgproduct variations id_category',
                    populate: {
                        path: 'id_category',
                        select: 'title'
                    }
                });

            console.log('Order found by ID:', order ? 'YES' : 'NO');
            if (order) {
                orders = [order];
            }
        } else {
            console.log('Searching by partial ID, text fields, or product names...');
            // Tìm kiếm theo một phần của ID, các trường khác, hoặc tên sản phẩm
            orders = await orderModel.aggregate([
                {
                    $lookup: {
                        from: 'users',
                        localField: 'id_user',
                        foreignField: '_id',
                        as: 'user'
                    }
                },
                {
                    $lookup: {
                        from: 'products',
                        localField: 'products.id_product',
                        foreignField: '_id',
                        as: 'productDetails'
                    }
                },
                {
                    $lookup: {
                        from: 'categories',
                        localField: 'productDetails.id_category',
                        foreignField: '_id',
                        as: 'categoryDetails'
                    }
                },
                {
                    $match: {
                        $or: [
                            { $expr: { $regexMatch: { input: { $toString: "$_id" }, regex: code, options: "i" } } },
                            { status: { $regex: code, $options: 'i' } },
                            { address: { $regex: code, $options: 'i' } },
                            { pay: { $regex: code, $options: 'i' } },
                            { total: { $regex: code, $options: 'i' } },
                            { 'productDetails.nameproduct': { $regex: code, $options: 'i' } }
                        ]
                    }
                },
                {
                    $limit: 10
                }
            ]);

            // Chuyển đổi kết quả aggregation về format tương tự như populate
            orders = orders.map(order => {
                const user = order.user[0] || {};
                const products = order.products.map(product => {
                    const productDetail = order.productDetails.find(p => p._id.toString() === product.id_product.toString()) || {};
                    const category = order.categoryDetails.find(c => c._id.toString() === productDetail.id_category?.toString()) || {};
                    
                    return {
                        ...product,
                        id_product: {
                            ...productDetail,
                            id_category: category
                        }
                    };
                });

                return {
                    ...order,
                    id_user: user,
                    products: products
                };
            });
        }

        console.log('Total orders found:', orders.length);
        res.json(orders);
    } catch (error) {
        console.error('Search orders error:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
});

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

router.put('/notification/mark-read/:id', async (req, res) => {
    try {
        const notificationId = req.params.id;
        
        const updatedNotification = await notificationModel.findByIdAndUpdate(
            notificationId,
            { isRead: true },
            { new: true }
        );

        if (!updatedNotification) {
            return res.status(404).json({ message: 'Không tìm thấy thông báo' });
        }

        return res.status(200).json({ 
            message: 'Đã đánh dấu đã đọc',
            notification: updatedNotification 
        });
    } catch (error) {
        console.error(error);
        return res.status(500).json({ message: 'Lỗi máy chủ nội bộ', error: error.message });
    }
});

// Cập nhật API GET notifications để chỉ trả về thông báo chưa đọc cho count
router.get('/notifications/unread/:userId', async (req, res) => {
    try {
        const userId = req.params.userId;
        
        const unreadNotifications = await notificationModel.find({
            userId: userId,
            isRead: false
        }).sort({ createdAt: -1 });

        return res.status(200).json(unreadNotifications);
    } catch (error) {
        console.error(error);
        return res.status(500).json({ message: 'Lỗi máy chủ nội bộ', error: error.message });
    }
});

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
});

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

        await orderModel.findByIdAndUpdate(
            orderId,
            updateData,
            { new: true, runValidators: true }
        );

        const order = await orderModel.findById(orderId)
            .populate('id_user')
            .populate('products.id_product');

        if (!order) {
            return res.status(404).json({ message: 'Không tìm thấy đơn hàng' });
        }

        if (status === 'Đang giao hàng') {
            const newNotification = new notificationModel({
                userId: order.id_user._id,
                title: 'Giao hàng thành công',
                content: `Đơn hàng <font color='#2196F3'>${order.id_order}</font> của bạn đã được giao thành công. Cảm ơn bạn đã mua sắm tại ShopBePoly!`,
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

router.get('/getStatusOder', async (req, res) => {
    try {
        const count = await orderModel.countDocuments({ status: 'Đang xử lý' });
        res.json({ count });
    } catch (error) {
        res.status(500).json({ error: 'Lỗi server khi đếm đơn hàng' });
    }
});

router.get('/api/list_comment/:userId', async (req, res) => {
    try {
        const cartItems = await cartModel.find({ id_user: req.params.userId });
        res.json(cartItems);
    } catch (error) {
        console.error('Lỗi', error);
        res.status(500).json({ error: 'Lỗi' });
    }
});

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
});

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
        res.send('Lỗi khi sửa');
    }
});




// API lấy danh sách user mà user hiện tại đã nhắn tin
router.get('/conversations/:userId', async (req, res) => {
    try {
        const { userId } = req.params;
        if (!mongoose.Types.ObjectId.isValid(userId)) {
            return res.status(400).json({ message: 'ID user không hợp lệ' });
        }

        // Tìm tất cả tin nhắn mà user là from hoặc to
        const conversations = await messageModel.aggregate([
            {
                $match: {
                    $or: [
                        { from: new mongoose.Types.ObjectId(userId) },
                        { to: new mongoose.Types.ObjectId(userId) }
                    ]
                }
            },
            {
                $group: {
                    _id: {
                        $cond: [
                            { $eq: ['$from', new mongoose.Types.ObjectId(userId)] },
                            '$to',
                            '$from'
                        ]
                    },
                    lastMessage: { $last: '$content' },
                    timestamp: { $last: '$timestamp' }
                }
            },
            {
                $lookup: {
                    from: 'users',
                    localField: '_id',
                    foreignField: '_id',
                    as: 'user'
                }
            },
            { $unwind: '$user' },
            {
                $project: {
                    userId: '$_id',
                    name: '$user.name',
                    avt_user: '$user.avt_user',
                    isOnline: '$user.isOnline',
                    lastMessage: 1,
                    timestamp: 1
                }
            },
            { $sort: { timestamp: -1 } }
        ]);

        res.json({
            message: conversations.length ? 'Lấy danh sách cuộc trò chuyện thành công' : 'Chưa có cuộc trò chuyện nào',
            data: conversations
        });
    } catch (err) {
        console.error('Lỗi lấy danh sách cuộc trò chuyện:', err.message);
        res.status(500).json({ message: 'Lỗi server', error: err.message });
    }
});

router.post('/send-message', async (req, res) => {
    try {
        const { from, to, content } = req.body;

        if (!mongoose.Types.ObjectId.isValid(from) || !mongoose.Types.ObjectId.isValid(to)) {
            return res.status(400).json({ error: 'ID from hoặc to không hợp lệ' });
        }

        const newMessage = new messageModel({
            from: new mongoose.Types.ObjectId(from),
            to: new mongoose.Types.ObjectId(to),
            content,
            timestamp: new Date()
        });
        await newMessage.save();

        const populatedMessage = await messageModel
            .findById(newMessage._id)
            .populate('from', 'name avt_user')
            .populate('to', 'name avt_user');

        // Push tin nhắn mới qua WebSocket đến người nhận (admin web)
        const recipientSocket = userSockets.get(to);
        if (recipientSocket && recipientSocket.readyState === WebSocket.OPEN) {
            recipientSocket.send(JSON.stringify({ type: 'new_message', data: populatedMessage }));
        }

        // Auto-reply nếu là tin nhắn đầu tiên đến admin
        const admin = await userModel.findOne({ _id: to, role: 2 });
        if (admin) {
            const hasReplied = await messageModel.exists({ from: to, to: from });
            if (!hasReplied) {
                const autoReply = new messageModel({
                    from: new mongoose.Types.ObjectId(to),
                    to: new mongoose.Types.ObjectId(from),
                    content: "Chào bạn! Bạn cần hỗ trợ gì?",
                    timestamp: new Date()
                });
                await autoReply.save();
                const populatedReply = await messageModel
                    .findById(autoReply._id)
                    .populate('from', 'name avt_user')
                    .populate('to', 'name avt_user');

                // Push auto-reply qua WebSocket đến người gửi (nếu online, nhưng vì app không WS, có thể bỏ hoặc push nếu user online)
                const senderSocket = userSockets.get(from);
                if (senderSocket && senderSocket.readyState === WebSocket.OPEN) {
                    senderSocket.send(JSON.stringify({ type: 'new_message', data: populatedReply }));
                }
            }
        }

        // Trả về response cho app
        res.status(200).json({ success: true, data: populatedMessage });
    } catch (err) {
        console.error('Error sending message:', err);
        res.status(500).json({ error: 'Lỗi server khi gửi tin nhắn' });
    }
});

router.get('/messages', async (req, res) => {
    try {
        const { userId, adminId } = req.query;
        if (!mongoose.Types.ObjectId.isValid(userId) || !mongoose.Types.ObjectId.isValid(adminId)) {
            return res.status(400).json({ error: 'ID user hoặc admin không hợp lệ' });
        }

        const messages = await messageModel.find({
            $or: [
                { from: new mongoose.Types.ObjectId(adminId), to: new mongoose.Types.ObjectId(userId) },
                { from: new mongoose.Types.ObjectId(userId), to: new mongoose.Types.ObjectId(adminId) }
            ]
        })
        .sort({ timestamp: 1 })  // Sắp xếp theo thời gian tăng dần
        .populate('from', 'name avt_user')  // Populate thông tin người gửi
        .populate('to', 'name avt_user');   // Populate thông tin người nhận

        res.json(messages);
    } catch (err) {
        console.error('Lỗi khi lấy tin nhắn:', err);
        res.status(500).json({ error: 'Lỗi server khi lấy tin nhắn' });
    }
});
// API gửi tin nhắn (fallback nếu không dùng WebSocket)
router.post('/messages', async (req, res) => {
    const { from, to, content } = req.body;
    if (!from || !to || !content) {
        return res.status(400).json({ message: 'Thiếu from, to hoặc content trong body' });
    }
    if (!mongoose.Types.ObjectId.isValid(from) || !mongoose.Types.ObjectId.isValid(to)) {
        return res.status(400).json({ message: 'ID from hoặc to không hợp lệ' });
    }

    try {
        const newMessage = new messageModel({
            from: new mongoose.Types.ObjectId(from),
            to: new mongoose.Types.ObjectId(to),
            content,
            timestamp: new Date()
        });
        await newMessage.save();

        const populatedMessage = await messageModel
            .findById(newMessage._id)
            .populate('from', 'name avt_user')
            .populate('to', 'name avt_user');

        // Gửi auto-reply nếu là tin nhắn đầu tiên đến admin
        const admin = await userModel.findOne({ _id: to, role: 2 });
        if (admin) {
            const hasReplied = await messageModel.exists({ from: to, to: from });
            if (!hasReplied) {
                const autoReply = new messageModel({
                    from: new mongoose.Types.ObjectId(to),
                    to: new mongoose.Types.ObjectId(from),
                    content: "Chào bạn! Bạn cần hỗ trợ gì?",
                    timestamp: new Date()
                });
                await autoReply.save();
            }
        }

        res.status(201).json({
            message: 'Gửi tin nhắn thành công',
            data: populatedMessage
        });
    } catch (err) {
        console.error('Lỗi khi gửi tin nhắn:', err.message);
        res.status(500).json({ message: 'Lỗi server khi gửi tin nhắn', error: err.message });
    }
});

router.get('/chat-users', async (req, res) => {
    try {
        const { adminId } = req.query;
        const users = await messageModel.aggregate([
            {
                $match: {
                    $or: [
                        { from: new mongoose.Types.ObjectId(adminId) },
                        { to: new mongoose.Types.ObjectId(adminId) }
                    ]
                }
            },
            {
                $group: {
                    _id: {
                        $cond: [
                            { $eq: ["$from", new mongoose.Types.ObjectId(adminId)] },
                            "$to",
                            "$from"
                        ]
                    }
                }
            },
            {
                $lookup: {
                    from: 'users',
                    localField: '_id',
                    foreignField: '_id',
                    as: 'userInfo'
                }
            },
            { $unwind: '$userInfo' },
            {
                $project: {
                    _id: '$userInfo._id',
                    name: '$userInfo.name',
                    avt_user: '$userInfo.avt_user',
                    isOnline: '$userInfo.isOnline'
                }
            }
        ]);
        res.json(users);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: 'Lỗi server khi lấy danh sách user chat' });
    }
});


router.get('/get-admin', async (req, res) => {
    try {
        const admin = await userModel.findOne({ role: 2 });
        if (!admin) {
            return res.status(404).json({ message: 'Admin not found' });
        }
        res.json({ _id: admin._id, name: admin.name, avt_user: admin.avt_user, isOnline: admin.isOnline });
    } catch (error) {
        console.error('Lỗi khi lấy admin:', error);
        res.status(500).json({ message: 'Server error' });
    }
});


router.put('/up_password/:id', async (req, res) => {
    try {
        const id = req.params.id;
        const { oldPassword, newPassword } = req.body;

        const user = await userModel.findById(id);
        if (!user) {
            return res.status(404).json({ message: 'Không tìm thấy người dùng' });
        }

        if (user.password !== oldPassword) {
            return res.status(400).json({ message: 'Mật khẩu cũ không đúng' });
        }

        user.password = newPassword;
        await user.save();

        res.json({ message: 'Đổi mật khẩu thành công' });
    } catch (error) {
        console.error('Lỗi đổi mật khẩu:', error);
        res.status(500).json({ message: 'Lỗi server khi đổi mật khẩu' });
    }
});

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

// Thống kê doanh thu
router.get('/statistics', async (req, res) => {
    try {
        const { start, end } = req.query;
        if (!start || !end) {
            return res.status(400).json({ message: 'Vui lòng cung cấp đầy đủ ngày bắt đầu và ngày kết thúc.' });
        }

        // Kiểm tra định dạng ngày (YYYY-MM-DD)
        if (!/^\d{4}-\d{2}-\d{2}$/.test(start) || !/^\d{4}-\d{2}-\d{2}$/.test(end)) {
            return res.status(400).json({ message: 'Định dạng ngày không hợp lệ.' });
        }

        // Tạo ngày với múi giờ UTC+07:00 bằng cách thêm offset
        const startDate = new Date(`${start}T00:00:00.000+07:00`);
        const endDate = new Date(`${end}T23:59:59.999+07:00`);
        if (isNaN(startDate.getTime()) || isNaN(endDate.getTime())) {
            return res.status(400).json({ message: 'Ngày không hợp lệ.' });
        }

        const results = await orderModel.aggregate([
            {
                $match: {
                    status: { $ne: 'Đã hủy' },
                    date: { $gte: startDate, $lte: endDate }
                }
            },
            {
                $group: {
                    _id: { $dateToString: { format: "%Y-%m-%d", date: "$date", timezone: "+07:00" } },
                    revenue: { $sum: { $toDouble: "$total" } }
                }
            },
            {
                $sort: { _id: 1 }
            },
            {
                $project: {
                    _id: 0,
                    label: "$_id",
                    revenue: "$revenue"
                }
            }
        ]);

        // Điền các ngày thiếu với doanh thu 0
        const dates = [];
        let currentDate = new Date(startDate);
        while (currentDate <= endDate) {
            dates.push(currentDate.toISOString().split('T')[0]);
            currentDate.setUTCDate(currentDate.getUTCDate() + 1); // Sử dụng UTC để tránh lệch múi giờ
        }

        const dataMap = new Map(results.map(item => [item.label, item.revenue]));
        const finalData = dates.map(date => ({
            label: date,
            revenue: dataMap.get(date) || 0
        }));

        res.json(finalData);
    } catch (error) {
        console.error('Lỗi thống kê doanh thu:', error);
        res.status(500).json({ message: 'Lỗi server khi thống kê doanh thu' });
    }
});

// Thống kê tổng quan
router.get('/statistics-overview', async (req, res) => {
    try {
        const { startDate, endDate } = req.query;
        const dateFilter = {};

        // Tạo ngày với múi giờ UTC+07:00
        if (startDate) {
            const start = new Date(`${startDate}T00:00:00.000+07:00`);
            if (isNaN(start.getTime())) return res.status(400).json({ message: 'Ngày bắt đầu không hợp lệ.' });
            dateFilter.$gte = start;
        }
        if (endDate) {
            const end = new Date(`${endDate}T23:59:59.999+07:00`);
            if (isNaN(end.getTime())) return res.status(400).json({ message: 'Ngày kết thúc không hợp lệ.' });
            dateFilter.$lte = end;
        }

        // Kiểm tra định dạng ngày (YYYY-MM-DD)
        if (startDate && !/^\d{4}-\d{2}-\d{2}$/.test(startDate) || endDate && !/^\d{4}-\d{2}-\d{2}$/.test(endDate)) {
            return res.status(400).json({ message: 'Định dạng ngày không hợp lệ.' });
        }

        const orderMatchStage = Object.keys(dateFilter).length ? { date: dateFilter } : {};

        const totalUsers = await userModel.countDocuments();
        const totalProducts = await productModel.countDocuments();
        const orderStats = await orderModel.aggregate([
            { $match: orderMatchStage },
            {
                $group: {
                    _id: null,
                    totalOrders: { $sum: 1 },
                    totalRevenue: { $sum: { $cond: [{ $ne: ["$status", "Đã hủy"] }, { $toDouble: "$total" }, 0] } },
                    countDelivered: { $sum: { $cond: [{ $eq: ["$status", "Đã giao"] }, 1, 0] } },
                    countProcessing: { $sum: { $cond: [{ $eq: ["$status", "Đang xử lý"] }, 1, 0] } },
                    countCancelled: { $sum: { $cond: [{ $eq: ["$status", "Đã hủy"] }, 1, 0] } },
                }
            }
        ]);

        const stats = orderStats[0] || {
            totalOrders: 0,
            totalRevenue: 0,
            countDelivered: 0,
            countProcessing: 0,
            countCancelled: 0,
        };

        res.json({
            totalProducts,
            totalUsers,
            totalOrders: stats.totalOrders,
            totalRevenue: stats.totalRevenue,
            countDelivered: stats.countDelivered,
            countProcessing: stats.countProcessing,
            countCancelled: stats.countCancelled,
        });
    } catch (error) {
        console.error('Lỗi thống kê tổng quan:', error);
        res.status(500).json({ message: 'Lỗi server khi lấy thống kê tổng quan' });
    }
});

router.get('/top-products', async (req, res) => {
    try {
        const { start, end } = req.query;
        if (!start || !end) {
            return res.status(400).json({ message: 'Vui lòng cung cấp đầy đủ ngày bắt đầu và ngày kết thúc.' });
        }

        if (!/^\d{4}-\d{2}-\d{2}$/.test(start) || !/^\d{4}-\d{2}-\d{2}$/.test(end)) {
            return res.status(400).json({ message: 'Định dạng ngày không hợp lệ.' });
        }

        const startDate = new Date(`${start}T00:00:00.000+07:00`);
        const endDate = new Date(`${end}T23:59:59.999+07:00`);
        if (isNaN(startDate.getTime()) || isNaN(endDate.getTime())) {
            return res.status(400).json({ message: 'Ngày không hợp lệ.' });
        }

        const topProducts = await orderModel.aggregate([
            {
                $match: {
                    status: 'Đã giao hàng', // Chỉ tính đơn hàng có trạng thái "Đã giao hàng"
                    date: { $gte: startDate, $lte: endDate }
                }
            },
            {
                $unwind: '$products'
            },
            {
                $group: {
                    _id: '$products.id_product',
                    totalQuantity: { $sum: '$products.quantity' },
                    productName: { $first: '$products.id_product.nameproduct' },
                    productImage: { $first: '$products.id_product.avt_imgproduct' }
                }
            },
            {
                $lookup: {
                    from: 'products',
                    localField: '_id',
                    foreignField: '_id',
                    as: 'productDetails'
                }
            },
            {
                $unwind: '$productDetails'
            },
            {
                $project: {
                    _id: 0,
                    productId: '$_id',
                    name: '$productDetails.nameproduct',
                    image: '$productDetails.avt_imgproduct',
                    totalQuantity: 1
                }
            },
            {
                $sort: { totalQuantity: -1 }
            },
            {
                $limit: 10
            }
        ]);

        res.json(topProducts);
    } catch (error) {
        console.error('Lỗi khi lấy top sản phẩm:', error);
        res.status(500).json({ message: 'Lỗi server khi lấy top sản phẩm' });
    }
});

// Lấy danh sách đơn hàng theo khoảng thời gian
router.get('/orders/by-range', async (req, res) => {
    try {
        const { start, end, status } = req.query;
        if (!start || !end) {
            return res.status(400).json({ message: 'Vui lòng cung cấp cả ngày bắt đầu và ngày kết thúc.' });
        }

        // Kiểm tra định dạng ngày (YYYY-MM-DD)
        if (!/^\d{4}-\d{2}-\d{2}$/.test(start) || !/^\d{4}-\d{2}-\d{2}$/.test(end)) {
            return res.status(400).json({ message: 'Định dạng ngày không hợp lệ.' });
        }

        // Tạo ngày với múi giờ UTC+07:00
        const startDate = new Date(`${start}T00:00:00.000+07:00`);
        const endDate = new Date(`${end}T23:59:59.999+07:00`);
        if (isNaN(startDate.getTime()) || isNaN(endDate.getTime())) {
            return res.status(400).json({ message: 'Ngày không hợp lệ.' });
        }

        const filter = {
            date: {
                $gte: startDate,
                $lte: endDate
            }
        };

        if (status) {
            filter.status = status;
        }

        const orders = await orderModel.find(filter)
            .sort({ date: -1 })
            .populate('id_user', 'name phone_number')
            .populate({
                path: 'products.id_product',
                select: 'nameproduct avt_imgproduct variations id_category',
                populate: {
                    path: 'id_category',
                    select: 'title'
                }
            });

        res.json(orders);
    } catch (error) {
        console.error('Lỗi khi lấy đơn hàng theo khoảng thời gian:', error);
        res.status(500).json({ message: 'Lỗi server khi lấy danh sách đơn hàng' });
    }
});

router.get('/banners', async (req, res) => {
    try {
        const banners = await Banner.find().sort({ createdAt: -1 });
        res.status(200).json(banners);
    } catch (error) {
        console.error('Lỗi khi lấy banners:', error);
        res.status(500).json({ message: 'Lỗi server khi lấy danh sách banner' });
    }
});

router.post('/banners', uploadBanner.single('image'), async (req, res) => {
    try {
        if (!req.file) {
            return res.status(400).json({ message: 'Vui lòng tải lên một file ảnh.' });
        }
        const { name } = req.body;
        if (!name || name.trim() === '') {
            await fs.unlink(req.file.path);
            return res.status(400).json({ message: 'Vui lòng cung cấp tên banner.' });
        }
        const imageUrl = `/uploads/${req.file.filename}`;
        const newBanner = new Banner({ name, imageUrl });
        await newBanner.save();
        res.status(201).json({ message: 'Thêm banner thành công!', banner: newBanner });
    } catch (error) {
        console.error('Lỗi khi thêm banner:', error);
        res.status(500).json({ message: 'Lỗi server khi thêm banner' });
    }
});

router.put('/banners/:id', uploadBanner.single('image'), async (req, res) => {
    try {
        const { id } = req.params;
        const { name } = req.body;
        
        // Kiểm tra validation
        if (!name && !req.file) {
            return res.status(400).json({ message: 'Vui lòng cung cấp tên banner mới hoặc file ảnh mới.' });
        }
        
        const banner = await Banner.findById(id);

        if (!banner) {
            if (req.file) await fs.unlink(req.file.path);
            return res.status(404).json({ message: 'Không tìm thấy banner.' });
        }

        if (name && name.trim() !== '') {
            banner.name = name;
        }

        if (req.file) {
            const oldFilePath = path.join(__dirname, banner.imageUrl);
            await fs.unlink(oldFilePath).catch(err => {
                console.error('Lỗi khi xóa file ảnh cũ:', err.message);
            });
            banner.imageUrl = `/uploads/${req.file.filename}`;
        }

        await banner.save();
        res.status(200).json({ message: 'Cập nhật banner thành công!', banner });
    } catch (error) {
        console.error('Lỗi khi cập nhật banner:', error);
        res.status(500).json({ message: 'Lỗi server khi cập nhật banner' });
    }
});

router.delete('/banners/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const banner = await Banner.findById(id);

        if (!banner) {
            return res.status(404).json({ message: 'Không tìm thấy banner.' });
        }

        const filePath = path.join(__dirname, banner.imageUrl);
        await fs.unlink(filePath).catch(err => {
            console.error('Lỗi khi xóa file ảnh:', err);
        });

        await Banner.findByIdAndDelete(id);
        res.status(200).json({ message: 'Xóa banner thành công!' });
    } catch (error) {
        console.error('Lỗi khi xóa banner:', error);
        res.status(500).json({ message: 'Lỗi server khi xóa banner' });
    }
});

app.use(express.json()); 